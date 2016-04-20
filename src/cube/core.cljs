(ns cube.core
  (:require [cube.world :refer [world-template update-world]]
            [cube.graphics :refer [render-fn]]
            [cljsjs.three]))

(enable-console-print!)

(defn ^:export build-snake [dom-element]
  (let [game-world (atom world-template)
        render (render-fn game-world "snake")]
    (defn render-loop []
      (js/requestAnimationFrame render-loop)
      (render))
    (render-loop)
    (js/setInterval #(swap! game-world update-world) 200)))