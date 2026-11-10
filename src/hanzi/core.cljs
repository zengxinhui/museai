(ns hanzi.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]
            [hanzi.data :as data]
            [hanzi.picks :as picks]
            [hanzi.storage :as store]
            [hanzi.ui :as ui]))

;; ---- State ---------------------------------------------------------------
;; db is the only persisted atom; a watch writes it to localStorage.
;; ui holds transient view / quiz state and is never persisted.
(defonce db (r/atom (store/load-db)))
(defonce ui (r/atom {:view :learn :learn-char nil :learn-tab :display :search "" :quiz nil :new-account ""}))

(add-watch db :persist
           (fn [_ _ _ new-db] (store/save-db! new-db)))

(defn current-name [] (:current @db))
(defn current-account [] (get-in @db [:accounts (current-name)]))
(defn state-of [ch] (store/char-state @db (current-name) ch))
(defn progress-of [ch] (store/account-progress @db (current-name) ch))

(defn current-batch []
  (take 5 (remove #(= :learned (state-of (:char %))) data/chars)))

(defn counts []
  (let [states (map #(state-of (:char %)) data/chars)]
    {:new (count (filter #(= :new %) states))
     :learning (count (filter #(= :learning %) states))
     :learned (count (filter #(= :learned %) states))}))

(defn due-chars []
  (let [now (js/Date.now)]
    (->> data/chars
         (filter (fn [c]
                   (let [p (progress-of (:char c))]
                     (and (= :learned (state-of (:char c)))
                          (:next-review p)
                          (<= (:next-review p) now)))))
         (sort-by (fn [c] (:next-review (progress-of (:char c))))))))

;; ---- Promotion -----------------------------------------------------------
(defn maybe-promote! [ch]
  (let [p (progress-of ch)]
    (when (and (= "learning" (:state p))
               (pos? (:read-pass p 0))
               (pos? (:write-pass p 0)))
      (let [now (js/Date.now)]
        (store/update-progress! db (current-name) ch
                                (fn [q] (assoc q :state "learned"
                                               :learned-at now
                                               :level 0
                                               :next-review (store/next-review-ms now 0))))))))

(defn mark-learning! [ch]
  ;; Studying or practising a character must never demote it: a
  ;; Learned character opened from Browse (to review its stroke
  ;; order) stays Learned when it is studied or its practice pad is
  ;; completed. Only New and Learning characters move to Learning.
  (when (not= :learned (state-of ch))
    (store/update-progress! db (current-name) ch
                            (fn [p] (assoc p :state "learning" :last-seen (js/Date.now))))))

(defn record-read-pass! [ch]
  (store/update-progress! db (current-name) ch
                          (fn [p] (update (assoc p :state "learning") :read-pass inc)))
  (maybe-promote! ch))

(defn record-write-pass! [ch]
  (store/update-progress! db (current-name) ch
                          (fn [p] (update (assoc p :state "learning") :write-pass inc)))
  (maybe-promote! ch))

;; A failed review no longer demotes the character to Learning.
;; Instead its interval resets to 1 day (level 0), staying Learned
;; and due again tomorrow.
(defn reset-review! [ch]
  (let [now (js/Date.now)]
    (store/update-progress! db (current-name) ch
                            (fn [q] (assoc q :state "learned"
                                           :level 0
                                           :next-review (store/next-review-ms now 0)
                                           :review-read-ok? false
                                           :last-seen now)))))

(defn record-review-pass! [ch]
  (let [p (progress-of ch)
        ;; Keep going up the ladder, but cap at the last interval (32 days).
        new-level (min (inc (:level p 0)) store/max-review-level)
        now (js/Date.now)]
    (store/update-progress! db (current-name) ch
                            (fn [q] (assoc q :state "learned"
                                           :reviews (inc (:reviews q 0))
                                           :level new-level
                                           :next-review (store/next-review-ms now new-level)
                                           :review-read-ok? false
                                           :last-seen now)))))

;; The read half of a review is committed as soon as it is answered,
;; not held until the write half (or the end of the session) is done.
;; The interval does not move yet: it only moves up when the write
;; half also passes (record-review-pass!), or resets when either half
;; fails (reset-review!). The flag is what lets a review interrupted
;; after the read half resume at the write half instead of starting
;; that character over.
(defn record-review-read-pass! [ch]
  (store/update-progress! db (current-name) ch
                          (fn [q] (assoc q :review-read-ok? true
                                         :last-seen (js/Date.now)))))

;; ---- Quizzes -------------------------------------------------------------
(defn make-question [ch-data]
  (let [others (shuffle (remove #(= (:char ch-data) (:char %)) data/chars))
        pinyin-opts (vec (shuffle
                          (cons (first (:pinyin ch-data))
                                (take 3 (distinct (map #(first (:pinyin %)) others))))))
        meaning-opts (vec (shuffle
                           (cons (:definition ch-data)
                                 (take 3 (distinct (map :definition others))))))]
    {:pinyin-opts pinyin-opts :meaning-opts meaning-opts}))

(defn review-resume-write? [kind ch-data]
  "True when a review of this character was interrupted after its
   read half had already passed, so the review resumes at writing
   instead of making the learner redo the reading."
  (and (= kind :review)
       (:review-read-ok? (progress-of (:char ch-data)))))

(defn start-quiz! [kind queue]
  (if (empty? queue)
    (swap! ui assoc :quiz {:kind kind :queue [] :pos 0 :empty? true})
    (let [first-ch (first queue)
          resume-write? (review-resume-write? kind first-ch)]
      (swap! ui assoc :quiz (merge {:kind kind :queue (vec queue) :pos 0
                                    :stage (if resume-write? :write :read)
                                    :answered? false
                                    :sel-pinyin nil :sel-meaning nil
                                    :read-ok? false :write-result nil
                                    ;; Per-character results for this session,
                                    ;; so the quiz shows progress as you go:
                                    ;; a passed character is ticked the
                                    ;; moment it is passed, not only when
                                    ;; the whole quiz finishes.
                                    :passed #{} :failed #{}}
                                   (when (or (= kind :read)
                                             (and (= kind :review) (not resume-write?)))
                                     (make-question first-ch)))))))

(defn quiz-char []
  (let [q (:quiz @ui)]
    (when (and q (< (:pos q) (count (:queue q))))
      (nth (:queue q) (:pos q)))))

(defn advance-quiz! []
  (let [q (:quiz @ui)
        next-pos (inc (:pos q))]
    (if (>= next-pos (count (:queue q)))
      (swap! ui assoc-in [:quiz :pos] next-pos)
      (let [next-ch (nth (:queue q) next-pos)
            resume-write? (review-resume-write? (:kind q) next-ch)]
        (swap! ui assoc :quiz (merge q {:pos next-pos
                                        :stage (if resume-write? :write :read)
                                        :answered? false
                                        :sel-pinyin nil :sel-meaning nil
                                        :read-ok? false :write-result nil}
                                     (when (or (= (:kind q) :read)
                                               (and (= (:kind q) :review) (not resume-write?)))
                                       (make-question next-ch))))))))

(defn submit-read! []
  (let [q (:quiz @ui)
        ch-data (quiz-char)
        correct-pinyin (first (:pinyin ch-data))
        ok? (and (= (:sel-pinyin q) correct-pinyin)
                 (= (:sel-meaning q) (:definition ch-data)))]
    (swap! ui assoc-in [:quiz :answered?] true)
    (swap! ui assoc-in [:quiz :read-ok?] ok?)
    ;; Session result, shown in the quiz progress strip as you go.
    ;; A read quiz character is decided here; a review character is
    ;; only decided as failed here (its pass waits for the write half).
    (when (or (= (:kind q) :read)
              (and (= (:kind q) :review) (not ok?)))
      (swap! ui update-in [:quiz (if ok? :passed :failed)]
             (fn [s] (conj (or s #{}) (:char ch-data)))))
    (cond
      (= (:kind q) :review)
      (if ok?
        (do (record-review-read-pass! (:char ch-data))
            (swap! ui assoc-in [:quiz :stage] :write))
        (do (reset-review! (:char ch-data))
            (swap! ui assoc-in [:quiz :stage] :failed)))
      ok? (record-read-pass! (:char ch-data)))))

(defn write-done! [results]
  (let [q (:quiz @ui)
        ch-data (quiz-char)
        total (count results)
        correct (count (filter :pass? results))
        pass? (and (pos? total)
                   (>= (/ correct total) 0.8)
                   (or (> total 2) (= correct total)))]
    (swap! ui assoc-in [:quiz :write-result] {:correct correct :total total :pass? pass?})
    ;; Session result, shown in the quiz progress strip as you go.
    ;; A write quiz character and a review character are both decided
    ;; here, when the writing is scored.
    (when (contains? #{:write :review} (:kind q))
      (swap! ui update-in [:quiz (if pass? :passed :failed)]
             (fn [s] (conj (or s #{}) (:char ch-data)))))
    (cond
      (= (:kind q) :review)
      (if pass?
        (record-review-pass! (:char ch-data))
        (reset-review! (:char ch-data)))
      pass? (record-write-pass! (:char ch-data)))))

;; ---- Small pieces --------------------------------------------------------
(defn state-badge [ch]
  (let [s (state-of ch)]
    [:span {:class (str "badge badge-" (name s))}
     (case s :new "New" :learning "Learning" :learned "Learned")]))

(defn char-header [ch-data]
  [:div
   [:div {:style {:display "flex" :align-items "baseline" :gap "16px" :flex-wrap "wrap"}}
    [:span {:style {:font-size "72px" :line-height "1"}} (:char ch-data)]
    [:span {:style {:font-size "28px"}} (clojure.string/join ", " (:pinyin ch-data))]
    [state-badge (:char ch-data)]]
   [:p (str "#" (:rank ch-data) " most frequent  ·  "
            (count (:strokes ch-data)) " strokes  ·  "
            "Radical " (:radical ch-data) "  ·  "
            "Structure " (:decomposition ch-data))]
   [:p [:strong "Meaning: "] (:definition ch-data)]
   (when (seq (:english-mtsu ch-data))
     [:p [:strong "MTSU gloss: "] (:english-mtsu ch-data)])
   [:p [:strong "MTSU pinyin: "] (:pinyin-mtsu ch-data)
    "  ·  " [:strong "Frequency: "] (:freq ch-data)]
   ])

(defn option-list [label options selected on-select]
  [:div {:style {:margin "12px 0"}}
   [:p [:strong label]]
   (for [opt options]
     ^{:key opt}
     [:label {:style {:display "block" :margin "6px 0" :cursor "pointer"
                      :padding "4px 8px" :border-radius "8px"
                      :background (when (= selected opt) "#ede9fe")}}
      [:input {:type "radio" :name label :checked (= selected opt)
               :on-change #(on-select opt)}]
      " " (if (> (count opt) 90) (str (subs opt 0 90) "…") opt)])])

(defn read-question-view []
  (let [q (:quiz @ui)
        ch-data (quiz-char)]
    [:div {:class "card"}
     [:h3 (str "Read: what is this character?  (" (inc (:pos q)) " of " (count (:queue q)) ")")]
     [:div {:style {:font-size "96px" :text-align "center"}} (:char ch-data)]
     [option-list "Pinyin" (:pinyin-opts q) (:sel-pinyin q)
      #(swap! ui assoc-in [:quiz :sel-pinyin] %)]
     [option-list "Meaning" (:meaning-opts q) (:sel-meaning q)
      #(swap! ui assoc-in [:quiz :sel-meaning] %)]
     (if (:answered? q)
       [:div
        [:p {:class (if (:read-ok? q) "feedback-correct" "feedback-wrong")}
         (if (:read-ok? q)
           "✓ Correct on both pinyin and meaning."
           (str "✗ Not quite. The answer is "
                (first (:pinyin ch-data)) " — " (:definition ch-data) "."))]
        (if (= (:stage q) :failed)
          [:div
           [:p "That review did not pass, so its interval resets to 1 day. It will be due again tomorrow."]
           [:button {:class "primary" :on-click advance-quiz!} "Next review"]]
          [:button {:class "primary" :on-click advance-quiz!} "Next"])]
       [:button {:class "primary"
                 :disabled (or (nil? (:sel-pinyin q)) (nil? (:sel-meaning q)))
                 :on-click submit-read!}
        "Check answer"])]))

(defn write-question-view [ch-data]
  (let [q (:quiz @ui)]
    [:div {:class "card"}
     [:h3 (str "Write: " (clojure.string/join ", " (:pinyin ch-data))
               " — " (:definition ch-data)
               "  (" (inc (:pos q)) " of " (count (:queue q)) ")")]
     [:p "Write the character from its pronunciation and meaning. No template is shown in a test. "
      "Strokes are checked in order, but loosely: get the direction generally right and it counts."]
     ^{:key (str "write-" (:char ch-data) "-" (:pos q))}
     [ui/writing-pad {:ch-data ch-data :mode :quiz :ghost? false
                      :on-complete write-done!}]
     (when (:write-result q)
       (let [{:keys [correct total pass?]} (:write-result q)]
         [:div
          [:p {:class (if pass? "feedback-correct" "feedback-wrong")}
           (str (if pass? "✓ Passed writing: " "✗ Writing needs more practice: ")
                correct " of " total " strokes correct.")]
          (when-not pass?
            [:p "This character stays in Learning. Practise it in Learn, then try the quiz again."])
          [:button {:class "primary" :on-click advance-quiz!} "Next"]]))]))

(defn quiz-progress-strip [q]
  "Per-character progress for this quiz session, updated the moment
   each character is passed or failed — not only when the quiz ends.
   A tick means that character's test was passed in this session;
   a cross means it was failed; a dot means it has not been tested yet."
  (let [passed (:passed q #{}) failed (:failed q #{}) queue (:queue q)]
    [:div {:style {:display "flex" :gap "6px" :flex-wrap "wrap" :align-items "center" :margin "8px 0"}}
     [:span (str "Passed " (count passed) " of " (count queue) " in this quiz: ")]
     (for [ch-data queue]
       ^{:key (:char ch-data)}
       [:span {:style {:font-size "20px"}}
        (:char ch-data)
        (cond (contains? passed (:char ch-data)) [:span {:class "tick-pass"} " ✓"]
              (contains? failed (:char ch-data)) [:span {:class "tick-fail"} " ✗"]
              :else [:span {:style {:color "#9ca3af"}} " ·"])])]))

(defn quiz-view [kind title blurb queue-fn]
  (let [q (:quiz @ui)]
    [:div
     [:h2 title] [:p blurb]
     (when (and q (seq (:queue q)))
       [quiz-progress-strip q])
     (cond
       (nil? q)
       [:button {:class "primary" :on-click #(start-quiz! kind (queue-fn))} "Start"]
       (:empty? q)
       [:div [:p "Nothing to test here yet."] [:button {:class "primary" :on-click #(swap! ui assoc :quiz nil)} "Back"]]
       (>= (:pos q) (count (:queue q)))
       [:div [:p "✓ Quiz finished."] [:button {:class "primary" :on-click #(swap! ui assoc :quiz nil)} "Back"]]
       (= kind :read) [read-question-view]
       (= kind :write) [write-question-view (quiz-char)]
       (= kind :review)
       (case (:stage q)
         :read [read-question-view]
         :write [write-question-view (quiz-char)]
         :failed [:div {:class "card"}
                  [:p "That review did not pass, so its interval resets to 1 day. It will be due again tomorrow."]
                  [:button {:class "primary" :on-click advance-quiz!} "Next review"]]
         [read-question-view])
       :else [:p "Unknown quiz."])]))

;; ---- Main views ----------------------------------------------------------
(defn learn-view []
  (let [batch (current-batch)
        ;; The selected character is looked up in all 500, not only
        ;; in the current batch, so a character opened from Browse —
        ;; including one already Learned — can be viewed here with
        ;; its stroke order and writing practice. The teaching batch
        ;; itself is unchanged: it is still the first 5 non-Learned.
        selected (or (some #(when (= (:char %) (:learn-char @ui)) %) data/chars)
                     (first batch))
        c (counts)
        tab (or (:learn-tab @ui) :display)]
    [:div
     [:h2 "Learn"]
     [:p (str "Learning in Keats order, at most 5 at a time. "
              "You have learned " (:learned c) " of 500, "
              (:learning c) " in progress, " (:new c) " still new. "
              (count (due-chars)) " learned character(s) are due for review.")]
     [:div {:class "batch"}
      (for [ch-data batch]
        ^{:key (:char ch-data)}
        [:button {:class (str "batch-btn" (when (= (:char ch-data) (:char selected)) " active"))
                  :on-click #(swap! ui assoc :learn-char (:char ch-data) :view :learn :learn-tab :display)}
         [:span {:style {:font-size "28px"}} (:char ch-data)]
         [:br] (first (:pinyin ch-data)) [:br] [state-badge (:char ch-data)]
         ;; Which half-tests this character has already passed, so a
         ;; pass in one quiz is visible here even before the other
         ;; half is passed and the character becomes Learned.
         [:br] [:small (str "Read " (if (pos? (:read-pass (progress-of (:char ch-data)) 0)) "✓" "·")
                            "  Write " (if (pos? (:write-pass (progress-of (:char ch-data)) 0)) "✓" "·"))]])]
     (if selected
       [:div {:class "card"}
        ;; Two tabs inside the character card, so the display and the
        ;; writing pad are never stacked on one long page: the user
        ;; switches between them instead of scrolling up and down.
        ;; The batch selector above and the action buttons below stay
        ;; visible in both tabs. Switching character resets to the
        ;; Character tab; switching tabs resets the pad/animation
        ;; (they remount), which is the expected tab behaviour.
        [:div {:class "subtabs"}
         [:button {:class (when (= tab :display) "active")
                   :on-click #(swap! ui assoc :learn-tab :display)}
          "Character"]
         [:button {:class (when (= tab :practice) "active")
                   :on-click #(swap! ui assoc :learn-tab :practice)}
          "Practise writing"]]
        (case tab
          :practice
          [:div
           [:h3 "Practise writing"]
           [:p "Trace the strokes in order. The faint shape is a hint — turn it off when you feel ready. "
            "A stroke counts when its direction is generally right; you can retry a stroke as often as you like here."]
           ^{:key (str "pad-" (:char selected))}
           [ui/writing-pad {:ch-data selected :mode :practice :ghost? true
                            :on-complete (fn [_] (mark-learning! (:char selected)))}]]
          [:div
           [char-header selected]
           [:h3 "Stroke order"]
           ^{:key (str "anim-" (:char selected))}
           [ui/stroke-animation selected]])
        [:div {:style {:margin-top "12px" :display "flex" :gap "8px" :flex-wrap "wrap"}}
         ;; No "move to Learning" button for a Learned character:
         ;; it is already Learned, and the button must not demote it.
         (when (not= :learned (state-of (:char selected)))
           [:button {:on-click #(mark-learning! (:char selected))}
            (if (= :new (state-of (:char selected)))
              "I've studied this — move it to Learning"
              "Keep this in Learning")])
         [:button {:on-click #(swap! ui assoc :view :read-quiz :quiz nil)} "Test reading"]
         [:button {:on-click #(swap! ui assoc :view :write-quiz :quiz nil)} "Test writing"]]]
       [:p "🎉 All 500 characters learned. Use Review to keep them fresh."])]))

;; ---- Pick of the Day -----------------------------------------------------
;; A pick is eligible when it uses at least 2 characters the current
;; account has Learned. The day's pick is deterministic from the
;; local calendar date, so it is the same all day and changes
;; tomorrow, with no server: index = (day + offset) mod pool-size.
;; :pick-offset (in the transient ui atom) lets the user browse to
;; another eligible pick without changing what tomorrow's pick is.
;; If no pick is eligible yet (fewer than 2 Learned characters in
;; any one pick), the full bank is used as a fallback so there is
;; always a pick, and the view says so.

(defn pick-texts [pick]
  (if (:lines pick) (map :text (:lines pick)) [(:text pick)]))

(defn pick-chars-ordered [pick]
  (let [known (set (map :char data/chars))]
    (distinct (filter #(contains? known (str %))
                      (mapcat seq (pick-texts pick))))))

(defn learned-char-set []
  (set (map :char (filter #(= :learned (state-of (:char %))) data/chars))))

(defn pick-day-number []
  (let [d (js/Date.)]
    (Math/floor (/ (js/Date.UTC (.getFullYear d) (.getMonth d) (.getDate d)) 86400000))))

(defn pick-of-day-view []
  (let [learned (learned-char-set)
        eligible (filter (fn [p]
                           (>= (count (filter #(contains? learned %) (pick-chars-ordered p))) 2))
                         picks/picks)
        fallback? (empty? eligible)
        pool (if fallback? picks/picks eligible)
        day (pick-day-number)
        idx (mod (+ day (:pick-offset @ui 0)) (count pool))
        pick (nth pool idx)
        chars (pick-chars-ordered pick)
        learned-in-pick (filter #(contains? learned %) chars)
        d (js/Date.)
        date-str (str (.getFullYear d) "-" (inc (.getMonth d)) "-" (.getDate d))]
    [:div
     [:h2 "Pick of the Day"]
     [:p (str "For " date-str ". "
              (if fallback?
                "No pick yet uses 2 characters you have Learned, so this is today's pick from the full bank. Learn its characters to unlock picks chosen from what you know."
                (str "This pick uses " (count learned-in-pick)
                     " character(s) you have Learned, out of " (count chars) " in the pick.")))]
     [:p (str (count eligible) " of " (count picks/picks)
              " picks use at least 2 characters you have Learned.")]
     [:div {:class "card"}
      (if (:lines pick)
        [:div
         [:p [:strong "Short conversation — "] (:meaning pick)]
         (for [line (:lines pick)]
           ^{:key (str (:speaker line) (:text line))}
           [:div {:style {:margin "12px 0"}}
            [:p [:strong (str (:speaker line) ": ")]
             [:span {:style {:font-size "28px"}} (:text line)]]
            [:p (:pinyin line)]
            [:p (:meaning line)]])]
        [:div
         [:p [:span {:style {:font-size "36px"}} (:text pick)]]
         [:p (:pinyin pick)]
         [:p (:meaning pick)]
         [:p (str (case (:kind pick) :phrase "Phrase" :sentence "Sentence" "Pick"))]])]
     [:div {:style {:display "flex" :gap "8px" :flex-wrap "wrap"}}
      [:button {:on-click #(swap! ui update :pick-offset (fn [o] (inc (or o 0))))}
       "Another pick"]]
     [:h3 "Characters in this pick"]
     [:div {:class "browse-grid"}
      (for [ch chars]
        (let [c (some #(when (= (:char %) ch) %) data/chars)]
          ^{:key ch}
          [:div {:class "browse-card"}
           [:span {:style {:font-size "28px"}} ch]
           [:br] (first (:pinyin c))
           [:br] [state-badge ch]
           [:br] [:small (:definition c)]]))]]))

(defn browse-view []
  (let [search (:search @ui)
        q (clojure.string/lower-case (or search ""))
        filtered (filter (fn [c]
                           (or (empty? q)
                               (clojure.string/includes? (:char c) q)
                               (clojure.string/includes? (clojure.string/lower-case
                                                          (clojure.string/join " " (:pinyin c))) q)
                               (clojure.string/includes? (clojure.string/lower-case (:definition c)) q)))
                         data/chars)
        batch-set (set (map :char (current-batch)))]
    [:div
     [:h2 "Browse all 500"]
     [:p "This is a reference list in Keats order. Click a character to open it in Learn — "
      "its stroke order and writing practice — including characters already Learned. "
      "Opening one does not change the teaching order or its state: it only enters Learn, "
      "quizzes and Review through the batch order."]
     [:input {:type "text" :placeholder "Search character, pinyin or meaning…"
              :value search :style {:width "100%" :padding "10px" :font-size "16px"}
              :on-change #(swap! ui assoc :search (.. % -target -value))}]
     [:p (str (count filtered) " characters shown.")]
     [:div {:class "browse-grid"}
      ;; Previously this was (take 200 filtered): the count above said
      ;; 500, but only the first 200 cards were actually rendered, so
      ;; Browse never reached the rest of the list. Render all of
      ;; filtered now — 500 simple cards is fine for React, and a
      ;; search narrows it further.
      (for [c filtered]
        ^{:key (:char c)}
        [:div {:class "browse-card"
               :title "Open in Learn"
               :style {:cursor "pointer"}
               :on-click #(swap! ui assoc :view :learn :learn-char (:char c) :learn-tab :display)}
         [:span {:style {:font-size "32px"}} (:char c)] [:br]
         (first (:pinyin c)) [:br]
         [:small (:definition c)] [:br]
         [state-badge (:char c)] [:br]
         [:small (if (contains? batch-set (:char c)) "In current batch" (str "#" (:rank c)))]])]]))

(defn handle-restore-file [e]
  (when-let [file (aget (.. e -target -files) 0)]
    (let [reader (js/FileReader.)]
      (set! (.-onload reader)
            (fn []
              (let [restored (store/parse-backup (.-result reader))]
                (if (nil? restored)
                  (swap! ui assoc :restore-msg
                         "Could not restore: that file is not a valid Hanzi Learner backup (EDN, version 1). Nothing was changed.")
                  (when (js/confirm (str "Restore backup? This will replace all "
                                         (count (:accounts @db))
                                         " account(s) currently in this browser with "
                                         (count (:accounts restored))
                                         " account(s) from the backup."))
                    (reset! db restored)
                    (swap! ui assoc :restore-msg
                           (str "Restored " (count (:accounts restored))
                                " account(s) from backup. Current account: "
                                (or (:current restored) "none") ".")))))))
      (.readAsText reader file))
    ;; Allow choosing the same file again after a restore attempt.
    (set! (.. e -target -value) "")))

(defn restore-control []
  [:div {:style {:margin-top "12px"}}
   [:p "Restore from a backup file (.edn) previously downloaded from this app. "
    "This replaces all accounts currently in this browser — download a fresh backup first if you want to keep them."]
   [:input {:type "file" :accept ".edn" :on-change handle-restore-file}]
   (when-let [msg (:restore-msg @ui)]
     [:p msg])])

(defn accounts-view []
  (let [names (sort (keys (:accounts @db)))
        new-name (:new-account @ui)]
    [:div
     [:h2 "Accounts"]
     [:p "Accounts live only in this browser's localStorage. There is no server account, no password, "
      "and nothing is uploaded. If you clear this browser's site data, these accounts go with it — "
      "use Download backup to keep a copy."]
     [:div {:class "card"}
      [:h3 "Switch account"]
      (for [n names]
        ^{:key n}
        [:div {:style {:display "flex" :gap "8px" :margin "6px 0" :align-items "center"}}
         [:span (str n (when (= n (current-name)) "  (current)"))]
         [:button {:on-click #(swap! db assoc :current n)} "Switch"]
         [:button {:on-click (fn []
                               (when (js/confirm (str "Delete account " n " and all its progress?"))
                                 (swap! db (fn [d]
                                             (let [d2 (update d :accounts dissoc n)]
                                               (assoc d2 :current (first (keys (:accounts d2)))))))))}
          "Delete"]])
      [:h3 "New account"]
      [:input {:type "text" :placeholder "Account name" :value new-name
               :on-change #(swap! ui assoc :new-account (.. % -target -value))}]
      [:button {:disabled (empty? (clojure.string/trim new-name))
                :on-click (fn []
                            (let [n (clojure.string/trim new-name)
                                  now (js/Date.now)]
                              (swap! db (fn [d] (-> d
                                                    (assoc-in [:accounts n] (store/new-account n now))
                                                    (assoc :current n))))
                              (swap! ui assoc :new-account "" :view :learn)))}
       "Create and switch"]
      [:h3 "Backup"]
      [:button {:on-click (fn []
                            (let [blob (js/Blob. #js [(pr-str @db)] #js {:type "application/edn"})
                                  url (.createObjectURL js/URL blob)
                                  a (.createElement js/document "a")]
                              (set! (.-href a) url)
                              (set! (.-download a) "hanzi-learner-backup.edn")
                              (.click a)
                              (.revokeObjectURL js/URL url)))}
       "Download backup (EDN)"]
      [restore-control]]]))

(defn account-setup []
  [:div {:class "card"}
   [:h2 "Welcome — make an account"]
   [:h3 "Restore from backup"]
   [restore-control]
   [:hr]
   [:p "This app keeps everything in this browser. Pick a name for your account to start learning the 500 most frequent characters."]
   [:input {:type "text" :placeholder "Your name"
            :value (:new-account @ui)
            :on-change #(swap! ui assoc :new-account (.. % -target -value))}]
   [:button {:disabled (empty? (clojure.string/trim (:new-account @ui)))
             :on-click (fn []
                         (let [n (clojure.string/trim (:new-account @ui))
                               now (js/Date.now)]
                           (swap! db (fn [d] (-> d
                                                 (assoc-in [:accounts n] (store/new-account n now))
                                                 (assoc :current n))))
                           (swap! ui assoc :new-account "" :view :learn)))}
    "Start learning"]])

(defn app []
  (if (nil? (current-name))
    [:div {:class "wrap"}
     [:header [:h1 "汉字 · Hanzi Learner"] [:p "Learn to read and write the 500 most frequent Chinese characters."]]
     [account-setup]]
    (let [c (counts)
          view (:view @ui)
          due (count (due-chars))]
      [:div {:class "wrap"}
       [:header
        [:h1 "汉字 · Hanzi Learner"]
        [:p (str "Account: " (current-name)
                 "  ·  Learned " (:learned c) "  ·  Learning " (:learning c)
                 "  ·  New " (:new c)
                 "  ·  Due for review: " due)]]
       [:nav
        (for [[v label] [[:learn "Learn"] [:read-quiz "Read quiz"] [:write-quiz "Write quiz"]
                         [:review (str "Review" (when (pos? due) (str " (" due ")")))]
                         [:browse "Browse"] [:pick "Pick of the Day"] [:accounts "Accounts"]]]
          ^{:key v}
          [:button {:class (str "nav-" (name v) (when (= view v) " active"))
                    :on-click #(swap! ui assoc :view v :quiz nil)}
           label])]
       [:main {:class (str "view-" (name view))}
        (case view
          :learn [learn-view]
          :read-quiz [quiz-view :read "Read quiz"
                      "Test the characters in your current batch (at most 5). Pick the pinyin and the meaning for each character."
                      (fn [] (filter #(= :learning (state-of (:char %))) (current-batch)))]
          :write-quiz [quiz-view :write "Write quiz"
                       "Test the characters in your current batch. Write from pinyin and meaning only — no template."
                       (fn [] (filter #(= :learning (state-of (:char %))) (current-batch)))]
          :review [quiz-view :review "Review learned characters"
                   "Learned characters come back after 1, 2, 4, 8, 16, then 32 days (32 is the upper limit). Pass reading and writing to go up a step; fail either and the interval resets to 1 day. Each character is saved as soon as you finish it — stop mid-session and the ones you have done stay done, and a character whose reading you already passed resumes at writing."
                   (fn [] (if (seq (due-chars)) (due-chars)
                            (filter #(= :learned (state-of (:char %))) data/chars)))]
          :browse [browse-view]
          :pick [pick-of-day-view]
          :accounts [accounts-view]
          [learn-view])]])))

;; React 19 (via Reagent 2.0.1) has no react-dom/render: the old
;; reagent.dom/render only works with React 18 or older, and silently
;; fails with React 19. Use the create-root API instead, keeping the
;; root in a defonce so a second init (e.g. hot reload) re-renders
;; into the same root instead of creating a second one.
(defonce app-root (atom nil))

(defn ^:export init []
  (let [root (or @app-root
                 (let [r (rdom-client/create-root
                           (.getElementById js/document "app"))]
                   (reset! app-root r)
                   r))]
    (rdom-client/render root [app])))
