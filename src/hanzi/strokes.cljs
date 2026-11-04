(ns hanzi.strokes
  "Geometry for makemeahanzi stroke data.

   graphics.txt stores each stroke as an SVG path in a coordinate system
   whose y-axis points up. The original svgs/ files render those paths
   inside <g transform=\"scale(1,-1) translate(0,-900)\">, i.e. the
   displayed y is 900 - y. The medians (stroke centrelines) use the
   same original coordinates, so display-median applies the same flip.
   Comparing a user's stroke with a median is done in display space,
   so the flip cancels for direction, but the flip is essential for
   drawing the character the right way up.")

(defn display-median
  "Convert one median (vector of [x y] in original coords) to display coords."
  [median]
  (mapv (fn [[x y]] [x (- 900 y)]) median))

(defn- dist [[x1 y1] [x2 y2]]
  (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2))))

(defn- centroid [pts]
  (let [n (max 1 (count pts))]
    [(/ (reduce + (map first pts)) n)
     (/ (reduce + (map second pts)) n)]))

(defn- avg [pts]
  (when (seq pts) (centroid pts)))

(defn- direction [pts]
  ;; Smooth the ends: average the first/last few points, so a shaky
  ;; start or finish does not decide the direction.
  (let [a (avg (take 3 pts))
        b (avg (take-last 3 pts))]
    (when (and a b) [(- (first b) (first a)) (- (second b) (second a))])))

(defn- path-length [pts]
  (reduce + 0 (map dist pts (rest pts))))

(defn evaluate-stroke
  "Relaxed stroke check, as requested: orientation is what matters.

   `user-pts` are display-space points from one pointer stroke,
   `median` is the expected median in original makemeahanzi coords.
   Returns {:pass? bool :angle-degrees n :reason string}.

   Pass when:
   - the stroke is a dot/short mark close to the median's centre, or
   - its overall direction is within ~70 degrees of the median's
     direction (cosine > 0.34) AND it is loosely near the median
     (centroid, start or end within 450 units on a 1024 grid).

   Stroke order and stroke count are handled by the caller: in
   practice mode a failed stroke is retried, in quiz mode a failed
   stroke is scored and the next stroke is attempted."
  [user-pts median]
  (let [med (display-median median)
        med-dir (direction med)
        user-dir (direction user-pts)
        len (path-length user-pts)
        med-cent (centroid med)
        user-cent (centroid user-pts)]
    (cond
      (< (count user-pts) 2)
      {:pass? false :angle-degrees nil :reason "That stroke was too short to read."}

      ;; Dot-like strokes (e.g. 丶): direction is meaningless, proximity is the test.
      (and (< len 60) (< (dist user-cent med-cent) 280))
      {:pass? true :angle-degrees 0 :reason "Dot in the right place."}

      (or (nil? med-dir) (nil? user-dir))
      {:pass? false :angle-degrees nil :reason "Could not read that stroke."}

      :else
      (let [[mx my] med-dir
            [ux uy] user-dir
            mn (Math/sqrt (+ (* mx mx) (* my my)))
            un (Math/sqrt (+ (* ux ux) (* uy uy)))
            cos (if (or (zero? mn) (zero? un))
                  0
                  (/ (+ (* mx ux) (* my uy)) (* mn un)))
            angle (* (/ 180 Math/PI) (Math/acos (max -1 (min 1 cos))))
            near? (or (< (dist user-cent med-cent) 400)
                      (< (dist (first user-pts) (first med)) 450)
                      (< (dist (last user-pts) (last med)) 450))]
        {:pass? (and (> cos 0.34) near?)
         :angle-degrees (Math/round angle)
         :reason (cond
                   (not near?) "Right direction, but in the wrong part of the box."
                   (<= cos 0.34) (str "Direction looks off by about " (Math/round angle) " degrees.")
                   :else "Direction looks right.")}))))

(defn points->polyline
  "Turn a vector of [x y] into an SVG polyline points string."
  [pts]
  (->> pts (map (fn [[x y]] (str x "," y))) (clojure.string/join " ")))

(defn interpolate-median
  "Point along a display median at progress t in [0,1], for animation."
  [median-display t]
  (let [pts median-display
        n (count pts)]
    (if (< n 2)
      (first pts)
      (let [pos (* t (dec n))
            i (min (dec n) (int (Math/floor pos)))
            f (- pos i)
            [x1 y1] (nth pts i)
            [x2 y2] (nth pts (min (dec n) (inc i)))]
        [(+ x1 (* f (- x2 x1))) (+ y1 (* f (- y2 y1)))]))))

(defn partial-median
  "Display-median points up to progress t, for drawing a growing stroke."
  [median-display t]
  (let [pts median-display
        n (count pts)]
    (if (<= t 0)
      [(first pts)]
      (let [pos (* t (dec n))
            i (min (dec n) (int (Math/floor pos)))
            head (subvec pts 0 (inc i))
            tip (interpolate-median pts t)]
        (if (= (last head) tip) head (conj head tip))))))
