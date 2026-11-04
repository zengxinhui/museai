(ns hanzi.storage
  "All user account data lives in this browser's localStorage.
   Nothing is sent to a server: there is no server in this app.
   The stored value is one EDN string, so no account data ever
   leaves the device that created it.

   Shape:
   {:version 1
    :current \"account-name\"
    :accounts {\"account-name\"
               {:name \"account-name\"
                :created-at 0
                :progress {\"char\" {:state \"learning\"|\"learned\"
                                    :read-pass n :write-pass n
                                    :reviews n :level n
                                    :learned-at ms :next-review ms
                                    :last-seen ms}}}}}
  "
  (:require [cljs.reader :as reader]
            [clojure.string :as str]))

(def storage-key "hanzi-learner-v1")

(def default-db {:version 1 :current nil :accounts {}})

(defn load-db []
  (try
    (if-let [raw (.getItem js/localStorage storage-key)]
      (let [db (reader/read-string raw)]
        (if (and (map? db) (= 1 (:version db))) db default-db))
      default-db)
    (catch :default _ default-db)))

(defn save-db! [db]
  (try
    (.setItem js/localStorage storage-key (pr-str db))
    (catch :default _ nil)))

(defn parse-backup
  "Parse a backup file's EDN text. Returns the db map if it looks like
   a Hanzi Learner backup (version 1, accounts map, current account
   either nil or present), otherwise nil. Used by Upload/Restore."
  [text]
  (try
    (let [db (reader/read-string text)]
      (when (and (map? db)
                 (= 1 (:version db))
                 (map? (:accounts db))
                 (or (nil? (:current db))
                     (contains? (:accounts db) (:current db))))
        (assoc db :current (or (:current db)
                               (first (keys (:accounts db)))))))
    (catch :default _ nil)))

(defn new-account [name now]
  {:name name :created-at now :progress {}})

(defn account-progress [db account-name ch]
  (get-in db [:accounts account-name :progress ch]))

(defn char-state
  "One of :new :learning :learned. Absence from progress means :new."
  [db account-name ch]
  (keyword (or (:state (account-progress db account-name ch)) "new")))

(defn update-progress! [db-atom account-name ch f]
  (swap! db-atom update-in [:accounts account-name :progress ch]
         (fn [p] (f (or p {:state "new" :read-pass 0 :write-pass 0
                           :reviews 0 :level 0})))))

;; Spaced review: intervals in days, by review level.
;; 1, 2, 4, 8, 16, 32 — 32 days is the upper limit: levels at or
;; above the last interval stay at 32 days, they do not grow further.
(def review-intervals [1 2 4 8 16 32])

(def max-review-level (dec (count review-intervals)))

(defn next-review-ms [now level]
  (+ now (* (nth review-intervals (min level max-review-level))
            24 60 60 1000)))

(defn- chinese-voice?
  "A voice that can speak Mandarin: zh-*, cmn-* (Mandarin), or zho-*."
  [v]
  (let [lang (or (.-lang v) "")]
    (or (str/starts-with? lang "zh")
        (str/starts-with? lang "cmn")
        (str/starts-with? lang "zho"))))

(defn- speak-utterance!
  [synth ch voice]
  (let [u (js/SpeechSynthesisUtterance. ch)]
    (set! (.-lang u) "zh-CN")
    (when voice (set! (.-voice u) voice))
    (set! (.-rate u) 0.9)
    (.speak synth u)))

(defn speak
  "Pronounce a character with the browser's built-in speech synthesis,
   if it is available. This is a browser API, not a library.
   Returns :ok, :no-speech, :no-zh-voice, or :waiting.
   - :waiting means the browser had not loaded its voice list yet
     (getVoices returns [] on first use in Chrome and others). In
     that case a voiceschanged handler speaks as soon as voices
     arrive, and calls on-done with :ok or :no-zh-voice then.
   - :no-zh-voice means voices are loaded but none can speak
     Chinese. The previous version returned success in this case
     and spoke with no voice, which is silent on many devices —
     that silent-success was hiding the real problem.
   Other fixes over the first version, which just called .speak:
   - only cancel when something is actually speaking or pending.
     Cancelling unconditionally and then speaking immediately is a
     known iOS failure: the new utterance gets swallowed by the
     cancel. When we do cancel, the new utterance is delayed 150 ms.
   - resume when paused (notably iOS Safari), otherwise queued
     utterances never play."
  [ch & [on-done]]
  (if (and js/window (.-speechSynthesis js/window) (seq ch))
    (try
      (let [synth (.-speechSynthesis js/window)
            was-busy (or (.-speaking synth) (.-pending synth))]
        (when was-busy (.cancel synth))
        (when (.-paused synth) (.resume synth))
        (let [voices (try (array-seq (.getVoices synth)) (catch :default _ nil))]
          (cond
            (empty? voices)
            (do
              (set! (.-onvoiceschanged synth)
                    (fn []
                      (set! (.-onvoiceschanged synth) nil)
                      (let [vs (try (array-seq (.getVoices synth)) (catch :default _ nil))
                            v (first (filter chinese-voice? vs))]
                        (if v
                          (do (speak-utterance! synth ch v)
                              (when on-done (on-done :ok)))
                          (when on-done (on-done :no-zh-voice))))))
              :waiting)
            :else
            (let [v (first (filter chinese-voice? voices))]
              (if (nil? v)
                :no-zh-voice
                (do
                  (if was-busy
                    (js/setTimeout #(speak-utterance! synth ch v) 150)
                    (speak-utterance! synth ch v))
                  :ok))))))
      (catch :default _ :no-speech))
    :no-speech))
