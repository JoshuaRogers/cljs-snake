(ns cube.behaviour
  (:require [cube.common :as common]))

(defn- behaviour-straight [{{nodes :nodes} :snake} candidate]
  "Visually the snake is painfully chaotic when clumped up. This attempts to straighten him to some degree."
  (if (>= (count nodes) 2)
    (let [head (first nodes)
          neck (nth nodes 1)
          direction (common/coordinate- head neck)]
      (if (= candidate (common/coordinate+ direction head)) 25))))

(defn- distance-between-points
  "The number of moves a snake would have to make to get from one position to another."
  [coordinate1 coordinate2]
  (let [offset (common/coordinate- coordinate1 coordinate2)
        pos-offset (common/coordinate-apply #(Math/abs %) offset)]
    (apply + (vals pos-offset))))

(defn- behaviour-hunger [{snake :snake dot :dot} candidate]
  (if-not (nil? dot)
    (let [ripeness (:ripeness dot)
          head (first (:nodes snake))
          head-dot-distance (distance-between-points head dot)
          candidate-dot-distance (distance-between-points candidate dot)]
      (cond
        (and (zero? candidate-dot-distance) (neg? ripeness)) -50
        (neg? ripeness) 0
        (< candidate-dot-distance head-dot-distance) ripeness
        :else (- ripeness)))))

(defn- behaviour-random [_ _]
  "Provides some degree of unpredictability in behvaviour"
  (rand 30))

(defn weigh-choice [world candidate]
  (let [behaviours (juxt behaviour-hunger behaviour-straight behaviour-random)]
    (apply + (remove nil? (behaviours world candidate)))))