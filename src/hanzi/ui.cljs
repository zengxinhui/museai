(ns hanzi.ui
  (:require [reagent.core :as r]
            [hanzi.strokes :as st]))

;; ---- Shared SVG pieces -------------------------------------------------
;; All stroke paths are drawn inside the makemeahanzi flip transform,
;; so a character appears the right way up (see hanzi.strokes).

(defn grid-lines []
  [:g {:stroke "#e5e7eb" :stroke-width 2 :stroke-dasharray "8,8"}
   [:line {:x1 0 :y1 0 :x2 1024 :y2 1024}]
   [:line {:x1 1024 :y1 0 :x2 0 :y2 1024}]
   [:line {:x1 512 :y1 0 :x2 512 :y2 1024}]
   [:line {:x1 0 :y1 512 :x2 1024 :y2 512}]
   [:rect {:x 2 :y 2 :width 1020 :height 1020 :fill "none" :stroke "#d1d5db"}]])

(defn char-svg
  "Render a character's strokes. `upto` limits how many strokes are shown
   (nil = all). Ghost strokes are faint, shown strokes are black."
  [{:keys [ch-data upto ghost?]}]
  (let [strokes (:strokes ch-data)
        n (if (nil? upto) (count strokes) (min upto (count strokes)))]
    [:svg {:viewBox "0 0 1024 1024" :width "100%" :style {:display "block"}}
     [grid-lines]
     [:g {:transform "scale(1,-1) translate(0,-900)"}
      (when ghost?
        (for [i (range (count strokes))]
          ^{:key (str "g" i)}
          [:path {:d (nth strokes i) :fill "#e5e7eb"}]))
      (for [i (range n)]
        ^{:key i}
        [:path {:d (nth strokes i) :fill "#111827"}])]]))

(defn stroke-animation
  "Step / play the stroke order. A dot travels along each stroke's
   median, in order. This is a self-contained Reagent component
   (form-2, so its animation atom survives re-renders)."
  [ch-data]
  (let [anim (r/atom {:idx 0 :t 0 :playing? false :timer nil})
        stop! (fn []
                (when-let [t (:timer @anim)] (js/clearTimeout t))
                (swap! anim assoc :playing? false :timer nil))
        tick! (fn tick! []
                (let [{:keys [idx t]} @anim
                      medians (:medians ch-data)]
                  (if (>= idx (count medians))
                    (stop!)
                    (if (>= t 1)
                      (do (swap! anim assoc :idx (inc idx) :t 0)
                          (swap! anim assoc :timer (js/setTimeout tick! 220)))
                      (do (swap! anim assoc :t (min 1 (+ t 0.09)))
                          (swap! anim assoc :timer (js/setTimeout tick! 35)))))))
        play! (fn []
                (stop!)
                (reset! anim {:idx 0 :t 0 :playing? true :timer nil})
                (swap! anim assoc :timer (js/setTimeout tick! 35)))]
    (fn [ch-data]
      (let [{:keys [idx t playing?]} @anim
            medians (:medians ch-data)
            done? (>= idx (count medians))]
        [:div
         [:div {:style {:max-width "340px" :margin "0 auto"}}
          [:svg {:viewBox "0 0 1024 1024" :width "100%" :style {:display "block"}}
           [grid-lines]
           [:g {:transform "scale(1,-1) translate(0,-900)"}
            (for [i (range (count (:strokes ch-data)))]
              ^{:key (str "g" i)}
              [:path {:d (nth (:strokes ch-data) i) :fill "#f3f4f6"}])
            (for [i (range (min idx (count (:strokes ch-data))))]
              ^{:key i}
              [:path {:d (nth (:strokes ch-data) i) :fill "#111827"}])
            (when (and (not done?) (< idx (count (:strokes ch-data))))
              [:path {:d (nth (:strokes ch-data) idx) :fill "#dbeafe"}])]
           (when (and (not done?) (< idx (count medians)))
             (let [med (st/display-median (nth medians idx))
                   pts (st/partial-median med t)
                   tip (st/interpolate-median med t)]
               [:g {:stroke "#2563eb" :stroke-width 30 :stroke-linecap "round"
                    :fill "none"}
                [:polyline {:points (st/points->polyline pts)}]
                [:circle {:cx (first tip) :cy (second tip) :r 34 :fill "#2563eb" :stroke "none"}]]))]]
         [:div {:style {:display "flex" :gap "8px" :justify-content "center" :margin-top "8px"}}
          [:button {:on-click play!} (if playing? "Restart" "▶ Play stroke order")]
          [:button {:on-click (fn []
                                (stop!)
                                ;; Bug fix: this used to cap idx at (dec count),
                                ;; so the last stroke could never advance to
                                ;; "Done" — Next just restarted the last stroke.
                                ;; Allow idx to reach (count medians), which is
                                ;; the done? state.
                                (swap! anim update :idx
                                       (fn [i] (min (count medians) (inc i))))
                                (swap! anim assoc :t 0))}
           "Next stroke"]
          [:button {:on-click (fn [] (stop!) (reset! anim {:idx 0 :t 0 :playing? false :timer nil}))}
           "Reset"]]
         [:p {:style {:text-align "center" :color "#6b7280"}}
          (if done?
            "Done — all strokes shown."
            (str "Stroke " (inc idx) " of " (count medians)
                 (when playing? " — playing…")))]]))))

;; ---- Writing pad -------------------------------------------------------
;; Pointer drawing in display (SVG) coordinates. One pointer stroke is
;; scored against the expected median for the current stroke index.

(defn writing-pad
  "Props: ch-data, mode (:practice or :quiz), ghost? (practice hint),
   on-complete (quiz only, called with a vector of stroke results).
   In practice mode a wrong stroke is removed so it can be retried;
   in quiz mode a wrong stroke is scored and the pad moves on."
  [{:keys [ch-data mode ghost? on-complete]}]
  (let [state (r/atom {:idx 0 :drawing? false :current []
                       :user-strokes [] :results [] :msg nil :done? false})
        svg-node (atom nil)
        to-svg (fn [e]
                 (when-let [node @svg-node]
                   (let [rect (.getBoundingClientRect node)]
                     [(* (/ (- (.-clientX e) (.-left rect)) (.-width rect)) 1024)
                      (* (/ (- (.-clientY e) (.-top rect)) (.-height rect)) 1024)])))
        finish-stroke! (fn []
                         (let [{:keys [idx current results]} @state
                               medians (:medians ch-data)]
                           (when (and (seq current) (< idx (count medians)))
                             (let [res (st/evaluate-stroke current (nth medians idx))
                                   results' (conj results res)
                                   pass? (:pass? res)]
                               (if (= mode :practice)
                                 (if pass?
                                   ;; Same last-stroke bug as the animation's
                                   ;; Next button: practice never set :done?
                                   ;; after the final stroke, so the pad said
                                   ;; "Draw stroke N+1 of N" and any further
                                   ;; stroke was silently dropped. Mark done
                                   ;; when the last stroke passes.
                                   (swap! state assoc
                                          :idx (inc idx) :current []
                                          :user-strokes (conj (:user-strokes @state) current)
                                          :results results'
                                          :done? (>= (inc idx) (count medians))
                                          :msg (str "Stroke " (inc idx) " ✓ — " (:reason res)))
                                   (swap! state assoc :current []
                                          :msg (str "Not quite — " (:reason res) " Try this stroke again.")))
                                 ;; quiz: score once, move on
                                 (do
                                   (swap! state assoc
                                          :idx (inc idx) :current []
                                          :user-strokes (conj (:user-strokes @state) current)
                                          :results results'
                                          :msg (str "Stroke " (inc idx) ": " (if pass? "✓" "✗")
                                                     " — " (:reason res)))
                                   (when (>= (inc idx) (count medians))
                                     (swap! state assoc :done? true)
                                     (when on-complete (on-complete results')))))))))]
    (fn [{:keys [ch-data mode ghost?]}]
      (let [{:keys [idx drawing? current user-strokes results msg done?]} @state
            total (count (:medians ch-data))
            correct (count (filter :pass? results))]
        [:div
         [:div {:style {:max-width "380px" :margin "0 auto" :touch-action "none"}}
          [:svg {:viewBox "0 0 1024 1024" :width "100%"
                 :ref (fn [n] (reset! svg-node n))
                 :style {:display "block" :background "#fff" :cursor "crosshair"}
                 :on-pointer-down (fn [e]
                                    (when-not done?
                                      (.preventDefault e)
                                      (when-let [pt (to-svg e)]
                                        (swap! state assoc :drawing? true :current [pt]))))
                 :on-pointer-move (fn [e]
                                    (when (and drawing? (not done?))
                                      (when-let [pt (to-svg e)]
                                        (swap! state update :current
                                               (fn [pts]
                                                 (if (and (seq pts)
                                                            (< (Math/sqrt
                                                                (+ (Math/pow (- (first pt) (first (last pts))) 2)
                                                                   (Math/pow (- (second pt) (second (last pts))) 2))) 8))
                                                   pts
                                                   (conj pts pt)))))))
                 :on-pointer-up (fn [_e]
                                  (when drawing?
                                    (swap! state assoc :drawing? false)
                                    (finish-stroke!)))
                 :on-pointer-cancel (fn [_e] (swap! state assoc :drawing? false :current []))}
           [grid-lines]
           [:g {:transform "scale(1,-1) translate(0,-900)"}
            (when ghost?
              (for [i (range (count (:strokes ch-data)))]
                ^{:key (str "g" i)}
                [:path {:d (nth (:strokes ch-data) i) :fill "#f3f4f6"}]))
            ;; In practice, a passed stroke is shown as the real stroke.
            (when (= mode :practice)
              (for [i (range (min idx (count (:strokes ch-data))))]
                ^{:key i}
                [:path {:d (nth (:strokes ch-data) i) :fill "#111827"}]))]
           ;; User ink (display space, no flip needed).
           [:g {:stroke "#2563eb" :stroke-width 30 :stroke-linecap "round"
                :stroke-linejoin "round" :fill "none"}
            (for [i (range (count user-strokes))]
              ^{:key (str "u" i)}
              [:polyline {:points (st/points->polyline (nth user-strokes i))
                          :stroke (if (:pass? (nth results i)) "#111827" "#2563eb")}])
            (when (seq current)
              [:polyline {:points (st/points->polyline current)}])]
           ;; Hint: where the next stroke starts.
           (when (and (not done?) (< idx (count (:medians ch-data))))
             (let [[x y] (first (st/display-median (nth (:medians ch-data) idx)))]
               [:circle {:cx x :cy y :r 26 :fill "#f59e0b"}]))]]
         [:p {:style {:text-align "center"}}
          (cond
            done? (str "Finished: " correct " of " total " strokes correct.")
            (= mode :practice) (str "Draw stroke " (inc idx) " of " total " — the orange dot is where it starts.")
            :else (str "Write stroke " (inc idx) " of " total " — no template in a test."))]
         (when msg [:p {:style {:text-align "center"}} msg])
         [:div {:style {:display "flex" :gap "8px" :justify-content "center"}}
          [:button {:on-click (fn [] (reset! state {:idx 0 :drawing? false :current []
                                                    :user-strokes [] :results [] :msg nil :done? false}))}
           "Clear and start again"]
          (when (= mode :quiz)
            [:button {:on-click (fn []
                                  (when-not done?
                                    (let [remaining (- total idx)
                                          padded (into results (repeat remaining
                                                                        {:pass? false :reason "Skipped."}))]
                                      (swap! state assoc :done? true :results padded)
                                      (when on-complete (on-complete padded)))))}
             "Give up on this character"])]]))))
