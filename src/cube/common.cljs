(ns cube.common)

(defn coordinate-apply [fn & args]
  "Applies a function to each component of one or more coordinates, returning a coordinate result."
  (let [apply-component #(apply fn (map % args))
        [x y z] (map apply-component [:x :y :z])]
    {:x x :y y :z z}))

(quote "Convenience methods for common apply functions")
(def coordinate+ (partial coordinate-apply +))
(def coordinate- (partial coordinate-apply -))
(def coordinate  (partial coordinate-apply identity))

(defn coordinate-valid? [world coord]
  "Checks that a coordinate lies inside the world boundary"
  (let [validity-test #(and (<= 0 %) (< % (:size world)))
        components (vals (coordinate-apply validity-test coord))]
    (every? true? components)))

(defn typeof-cell [world test-coordinate]
  "Returns the type of the cell at a given position."
  (let [{dot :dot snake :snake} world
        test-coords (coordinate test-coordinate)
        snake-coords (map coordinate (:nodes snake))]

    (cond (not (coordinate-valid? world test-coords)) :invalid
          (= test-coords (coordinate dot))            :dot
          (some #(= test-coords %) snake-coords)      :snake
          :else                                       :empty)))

(defn cells [size]
  (flatten
    (for [x (range 0 size)]
      (for [y (range 0 size)]
        (for [z (range 0 size)]
          {:x x :y y :z z})))))
