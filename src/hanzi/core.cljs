(ns hanzi.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdom-client]
            [hanzi.data :as data]
            [hanzi.storage :as store]
            [hanzi.ui :as ui]))

;; ---- State ---------------------------------------------------------------
;; db is the only persisted atom; a watch writes it to localStorage.
;; ui holds transient view / quiz state and is never persisted.
(defonce db (r/atom (store/load-db)))
(defonce ui (r/atom {:view :learn :learn-char nil :search "" :quiz nil :new-account ""}))

(add-watch db :persist
           (fn [_ _ _ new-db] (store/save-db! new-db)))

(defn current-name [] (:current @db))
(defn current-account [] (get-in @db [:accounts (current-name)]))
(defn state-of [ch] (store/char-state @db (current-name) ch))
(defn progress-of [ch] (store/account-progress @db (current-name) ch))

(defn current-batch []
  (take 10 (remove #(= :learned (state-of (:char %))) data/chars)))

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
  (store/update-progress! db (current-name) ch
                          (fn [p] (assoc p :state "learning" :last-seen (js/Date.now)))))

(defn record-read-pass! [ch]
  (store/update-progress! db (current-name) ch
                          (fn [p] (update (assoc p :state "learning") :read-pass inc)))
  (maybe-promote! ch))

(defn record-write-pass! [ch]
  (store/update-progress! db (current-name) ch
                          (fn [p] (update (assoc p :state "learning") :write-pass inc)))
  (maybe-promote! ch))

(defn demote-to-learning! [ch]
  (store/update-progress! db (current-name) ch
                          (fn [p] (assoc p :state "learning" :level 0 :next-review nil))))

(defn record-review-pass! [ch]
  (let [p (progress-of ch)
        new-level (inc (:level p 0))
        now (js/Date.now)]
    (store/update-progress! db (current-name) ch
                            (fn [q] (assoc q :state "learned"
                                           :reviews (inc (:reviews q 0))
                                           :level new-level
                                           :next-review (store/next-review-ms now new-level)
                                           :last-seen now)))))

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

(defn start-quiz! [kind queue]
  (if (empty? queue)
    (swap! ui assoc :quiz {:kind kind :queue [] :pos 0 :empty? true})
    (let [first-ch (first queue)]
      (swap! ui assoc :quiz (merge {:kind kind :queue (vec queue) :pos 0
                                    :stage :read :answered? false
                                    :sel-pinyin nil :sel-meaning nil
                                    :read-ok? false :write-result nil}
                                   (when (or (= kind :read) (= kind :review))
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
      (let [next-ch (nth (:queue q) next-pos)]
        (swap! ui assoc :quiz (merge q {:pos next-pos :stage :read :answered? false
                                        :sel-pinyin nil :sel-meaning nil
                                        :read-ok? false :write-result nil}
                                     (when (or (= (:kind q) :read) (= (:kind q) :review))
                                       (make-question next-ch))))))))

(defn submit-read! []
  (let [q (:quiz @ui)
        ch-data (quiz-char)
        correct-pinyin (first (:pinyin ch-data))
        ok? (and (= (:sel-pinyin q) correct-pinyin)
                 (= (:sel-meaning q) (:definition ch-data)))]
    (swap! ui assoc-in [:quiz :answered?] true)
    (swap! ui assoc-in [:quiz :read-ok?] ok?)
    (cond
      (= (:kind q) :review)
      (if ok?
        (swap! ui assoc-in [:quiz :stage] :write)
        (do (demote-to-learning! (:char ch-data))
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
    (cond
      (= (:kind q) :review)
      (if pass?
        (record-review-pass! (:char ch-data))
        (demote-to-learning! (:char ch-data)))
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
   [:button {:on-click #(store/speak (:char ch-data))} "🔊 Hear it"]])

(defn option-list [label options selected on-select]
  [:div {:style {:margin "12px 0"}}
   [:p [:strong label]]
   (for [opt options]
     ^{:key opt}
     [:label {:style {:display "block" :margin "6px 0" :cursor "pointer"}}
      [:input {:type "radio" :name label :checked (= selected opt)
               :on-change #(on-select opt)}]
      " " (if (> (count opt) 90) (str (subs opt 0 90) "…") opt)])])

(defn read-question-view []
  (let [q (:quiz @ui)
        ch-data (quiz-char)]
    [:div {:class "card"}
     [:h3 (str "Read: what is this character?  (" (inc (:pos q)) " of " (count (:queue q)) ")")]
     [:div {:style {:font-size "96px" :text-align "center"}} (:char ch-data)]
     [:div {:style {:display "flex" :gap "8px" :justify-content "center"}}
      [:button {:on-click #(store/speak (:char ch-data))} "🔊 Hear it"]]
     [option-list "Pinyin" (:pinyin-opts q) (:sel-pinyin q)
      #(swap! ui assoc-in [:quiz :sel-pinyin] %)]
     [option-list "Meaning" (:meaning-opts q) (:sel-meaning q)
      #(swap! ui assoc-in [:quiz :sel-meaning] %)]
     (if (:answered? q)
       [:div
        [:p (if (:read-ok? q)
              "✓ Correct on both pinyin and meaning."
              (str "✗ Not quite. The answer is "
                   (first (:pinyin ch-data)) " — " (:definition ch-data) "."))]
        (if (= (:stage q) :failed)
          [:div
           [:p "This character goes back to Learning, so it will come up again in your current batch."]
           [:button {:on-click advance-quiz!} "Next review"]]
          [:button {:on-click advance-quiz!} "Next"])]
       [:button {:disabled (or (nil? (:sel-pinyin q)) (nil? (:sel-meaning q)))
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
          [:p (str (if pass? "✓ Passed writing: " "✗ Writing needs more practice: ")
                   correct " of " total " strokes correct.")]
          (when-not pass?
            [:p "This character stays in Learning. Practise it in Learn, then try the quiz again."])
          [:button {:on-click advance-quiz!} "Next"]]))]))

(defn quiz-view [kind title blurb queue-fn]
  (let [q (:quiz @ui)]
    [:div
     [:h2 title] [:p blurb]
     (cond
       (nil? q)
       [:button {:on-click #(start-quiz! kind (queue-fn))} "Start"]
       (:empty? q)
       [:div [:p "Nothing to test here yet."] [:button {:on-click #(swap! ui assoc :quiz nil)} "Back"]]
       (>= (:pos q) (count (:queue q)))
       [:div [:p "✓ Quiz finished."] [:button {:on-click #(swap! ui assoc :quiz nil)} "Back"]]
       (= kind :read) [read-question-view]
       (= kind :write) [write-question-view (quiz-char)]
       (= kind :review)
       (case (:stage q)
         :read [read-question-view]
         :write [write-question-view (quiz-char)]
         :failed [:div {:class "card"}
                  [:p "That review did not pass, so the character is back in Learning."]
                  [:button {:on-click advance-quiz!} "Next review"]]
         [read-question-view])
       :else [:p "Unknown quiz."])]))

;; ---- Main views ----------------------------------------------------------
(defn learn-view []
  (let [batch (current-batch)
        selected (or (some #(when (= (:char %) (:learn-char @ui)) %) batch)
                     (first batch))
        c (counts)]
    [:div
     [:h2 "Learn"]
     [:p (str "Learning in frequency order, at most 10 at a time. "
              "You have learned " (:learned c) " of 1000, "
              (:learning c) " in progress, " (:new c) " still new. "
              (count (due-chars)) " learned character(s) are due for review.")]
     [:div {:class "batch"}
      (for [ch-data batch]
        ^{:key (:char ch-data)}
        [:button {:class (str "batch-btn" (when (= (:char ch-data) (:char selected)) " active"))
                  :on-click #(swap! ui assoc :learn-char (:char ch-data) :view :learn)}
         [:span {:style {:font-size "28px"}} (:char ch-data)]
         [:br] (first (:pinyin ch-data)) [:br] [state-badge (:char ch-data)]])]
     (if selected
       [:div {:class "card"}
        [char-header selected]
        [:h3 "Stroke order"]
        ^{:key (str "anim-" (:char selected))}
        [ui/stroke-animation selected]
        [:h3 "Practise writing"]
        [:p "Trace the strokes in order. The faint shape is a hint — turn it off when you feel ready. "
         "A stroke counts when its direction is generally right; you can retry a stroke as often as you like here."]
        ^{:key (str "pad-" (:char selected))}
        [ui/writing-pad {:ch-data selected :mode :practice :ghost? true
                         :on-complete (fn [_] (mark-learning! (:char selected)))}]
        [:div {:style {:margin-top "12px" :display "flex" :gap "8px" :flex-wrap "wrap"}}
         [:button {:on-click #(mark-learning! (:char selected))}
          (if (= :new (state-of (:char selected)))
            "I've studied this — move it to Learning"
            "Keep this in Learning")]
         [:button {:on-click #(swap! ui assoc :view :read-quiz :quiz nil)} "Test reading"]
         [:button {:on-click #(swap! ui assoc :view :write-quiz :quiz nil)} "Test writing"]]]
       [:p "🎉 All 1000 characters learned. Use Review to keep them fresh."])]))

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
     [:h2 "Browse all 1000"]
     [:p "This is a reference list in frequency order. Teaching itself stays in batches of 10 — "
      "a character outside the current batch can be looked up here, but it only enters Learn, quizzes and Review through the batch order."]
     [:input {:type "text" :placeholder "Search character, pinyin or meaning…"
              :value search :style {:width "100%" :padding "10px" :font-size "16px"}
              :on-change #(swap! ui assoc :search (.. % -target -value))}]
     [:p (str (count filtered) " characters shown.")]
     [:div {:class "browse-grid"}
      (for [c (take 200 filtered)]
        ^{:key (:char c)}
        [:div {:class "browse-card"}
         [:span {:style {:font-size "32px"}} (:char c)] [:br]
         (first (:pinyin c)) [:br]
         [:small (:definition c)] [:br]
         [state-badge (:char c)] [:br]
         [:small (if (contains? batch-set (:char c)) "In current batch" (str "#" (:rank c)))]])]]))

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
       "Download backup (EDN)"]]]))

(defn account-setup []
  [:div {:class "card"}
   [:h2 "Welcome — make an account"]
   [:p "This app keeps everything in this browser. Pick a name for your account to start learning the 1000 most frequent characters."]
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
     [:header [:h1 "汉字 · Hanzi Learner"] [:p "Learn to read and write the 1000 most frequent Chinese characters."]]
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
                         [:browse "Browse"] [:accounts "Accounts"]]]
          ^{:key v}
          [:button {:class (when (= view v) "active")
                    :on-click #(swap! ui assoc :view v :quiz nil)}
           label])]
       [:main
        (case view
          :learn [learn-view]
          :read-quiz [quiz-view :read "Read quiz"
                      "Test the characters in your current batch (at most 10). Pick the pinyin and the meaning for each character."
                      (fn [] (filter #(= :learning (state-of (:char %))) (current-batch)))]
          :write-quiz [quiz-view :write "Write quiz"
                       "Test the characters in your current batch. Write from pinyin and meaning only — no template."
                       (fn [] (filter #(= :learning (state-of (:char %))) (current-batch)))]
          :review [quiz-view :review "Review learned characters"
                   "Learned characters come back after 1, 3, 7, 14, 30, then 60 days. Pass reading and writing to push the next review out; fail either and the character returns to Learning."
                   (fn [] (if (seq (due-chars)) (due-chars)
                            (filter #(= :learned (state-of (:char %))) data/chars)))]
          :browse [browse-view]
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
