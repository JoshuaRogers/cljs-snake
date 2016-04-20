(ns cube.world
  (:require [cube.common :as common]
            [cube.behaviour :as behaviour]))

(def world-template {:snake {:state :alive
                             :nodes '({:x 0 :y 0 :z 3}
                                      {:x 0 :y 0 :z 2}
                                      {:x 0 :y 0 :z 1}
                                      {:x 0 :y 0 :z 0})}
                     :dot nil
                     :size 4
                     :ticks 0})

(defn neighbors [coord]
  "Generates of a list of all the neighboring cells for a given coordinate."
  (map (partial common/coordinate+ coord)
       [{:x -1 :y  0 :z  0}
        {:x  1 :y  0 :z  0}
        {:x  0 :y -1 :z  0}
        {:x  0 :y  1 :z  0}
        {:x  0 :y  0 :z -1}
        {:x  0 :y  0 :z  1}]))

(defn valid-moves [world]
  (let [head (first (get-in world [:snake :nodes]))
        candidates (neighbors head)]
    (filter #(contains? #{:empty :dot} (common/typeof-cell world %))
            candidates)))

(defn empty-cells [world]
  (filter #(= (common/typeof-cell world %) :empty)
          (common/cells (:size world))))

(defn find-best-move [world]
  "Finds the move the makes the snake happiest given his behaviours."
  (let [moves (valid-moves world)
        ordered-moves (sort-by #(behaviour/weigh-choice world %) moves)]
    (-> ordered-moves reverse first)))

(defn update-dot [world]
  (cond
    (not (nil? (:dot world))) (update-in world [:dot :ripeness] inc)
    (pos? (mod (:ticks world) 256)) world
    :else (assoc-in world [:dot] (assoc (rand-nth (empty-cells world)) :ripeness -100))))

(defn- update-snake-move [world]
  (let [best-move (find-best-move world)
        nodes (get-in world [:snake :nodes])
        node-count (count nodes)
        moved-snake (concat [best-move] nodes)]
    (if (= (common/typeof-cell world best-move) :dot)
      (-> world
          (assoc-in [:snake :nodes] moved-snake)
          (assoc-in [:dot] nil))
      (assoc-in world [:snake :nodes] (take node-count moved-snake)))))

(defn- update-snake-alive [world]
  (if (empty? (valid-moves world))
    (assoc-in world [:snake :state] :dead)
    (update-snake-move world)))

(defn- update-snake-dead [world]
  "Decomposes the snake"
  (if (empty? (get-in world [:snake :nodes]))
    world-template
    (update-in world [:snake :nodes] rest)))

(defn update-snake [world]
  "Returns a world where the snake has moved."
  (if (= (get-in world [:snake :state]) :alive)
    (update-snake-alive world)
    (update-snake-dead world)))

(defn update-ticks [world]
  "Updates the tick count of the world"
  (update-in world [:ticks] inc))

(defn update-world [world]
  (-> world
      update-dot
      update-ticks
      update-snake))
