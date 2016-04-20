(ns cube.graphics
  (:require [cube.common :refer [coordinate-apply coordinate cells]]))

(defn hsv [hue saturation value]
  (let [h1 (/ hue 60)
        f (mod h1 1)
        v1 (* value 255)
        pv (* (- 1 saturation) v1)
        qv (* (- 1 (* saturation f)) v1)
        tv (* (- 1 (* saturation (- 1 f))) v1)
        opt (mod (int h1) 6)]
    (case opt
      0 { :red v1 :green tv :blue pv }
      1 { :red qv :green v1 :blue pv }
      2 { :red pv :green v1 :blue tv }
      3 { :red pv :green qv :blue v1 }
      4 { :red tv :green pv :blue v1 }
      5 { :red v1 :green pv :blue qv })))

(defn- update-material! [mesh color opacity]
  (.setRGB (aget mesh "material" "color") (/ (:red color) 255) (/ (:green color) 255) (/ (:blue color) 255))
  (aset mesh "material" "opacity" opacity))

(defn- default-material! [mesh]
  (update-material! mesh {:red 0 :green 0 :blue 0} 0.03125))

(defn cube-mesh [{x :x y :y z :z}]
  (let [cube-geometry (js/THREE.BoxGeometry. 0.5 0.5 0.5)
        cube-material (js/THREE.MeshBasicMaterial. (js-obj "transparent" true))
        cube (js/THREE.Mesh. cube-geometry cube-material)]
    (doto cube
      (default-material!)
      (aset "position" "x" x)
      (aset "position" "y" y)
      (aset "position" "z" z))))

(defn cubes [size]
  (map #(assoc % :mesh (cube-mesh %)) (cells size)))

(defn- renderer-update-camera [camera]
  (let [pitch (atom 0.0)
        yaw (atom 0.0)]
    (fn []
      (swap! pitch #(mod (+ % 0.12) 360))
      (swap! yaw #(mod (+ % 0.2) 360))
      (aset camera "position" "z" (* 6 (Math/cos (js/THREE.Math.degToRad (deref yaw)))))
      (aset camera "position" "x" (* 6 (Math/sin (js/THREE.Math.degToRad (deref yaw)))))
      (aset camera "position" "y" (+ 1.75 (* 3 (Math/sin (js/THREE.Math.degToRad (deref pitch))))))
      (.lookAt camera (js-obj "x" 1.75 "y" 1.75 "z" 1.75)))))

(defn- renderer-update-cubes [display-entities game]
  (fn []
    (doseq [cube (map :mesh display-entities)] (default-material! cube))
    (let [{ticks :ticks snake :snake dot :dot} (deref game)
          nodes (:nodes snake)
          indexed-nodes (map vector (range) nodes)]
      (doseq [[index node] indexed-nodes]
        (let [mesh (first (filter #(= (coordinate %) (coordinate node)) display-entities))]
          (update-material! (:mesh mesh) (hsv (mod (* 4 (- ticks index)) 360) 1 1) 0.6)))
      (let [mesh (first (filter #(= (coordinate %) (coordinate dot)) display-entities))
            ripeness (Math/min (/ (+ 100 (:ripeness dot)) 100) 1)
            multiplier (if (pos? (:ripeness dot)) 16 8)]
        (if-not (nil? mesh)
          (update-material! (:mesh mesh) (hsv (mod (* multiplier ticks) 360) ripeness 1) 0.75))))))

(defn- resize-screen! [dom-element camera renderer]
  (let [width (aget (.getElementById js/document dom-element) "clientWidth")
        height (aget (.getElementById js/document dom-element) "clientHeight")]
    (.setSize renderer width height)
    (aset camera "aspect" (/ width height))
    (.updateProjectionMatrix camera)))

(defn- renderer-draw [dom-element scene camera]
  (let [webgl-renderer (js/THREE.WebGLRenderer. (js-obj "alpha" true "antialias" true))
        resize-fn #(resize-screen! dom-element camera webgl-renderer)]
    (doto webgl-renderer (.setClearColor 0xFFFFFF 0))
    (.appendChild (.getElementById js/document dom-element) (aget webgl-renderer "domElement"))

    (js/addEventListener "resize" resize-fn)
    (resize-fn)

    #(.render webgl-renderer scene camera)))

(defn render-fn [game dom-element]
  (let [scene (js/THREE.Scene. (js-obj "color" 0xFFFFFF))
        camera (js/THREE.PerspectiveCamera. 75 1 0.1 1000)
        cube-size (:size (deref game))
        display-entities (cubes cube-size)
        renderer-update-camera-fn (renderer-update-camera camera)
        renderer-update-cubes-fn (renderer-update-cubes display-entities game)
        renderer-draw-fn (renderer-draw dom-element scene camera)]
    (doseq [cube (map :mesh display-entities)] (.add scene cube))
    (fn []
      (renderer-update-camera-fn)
      (renderer-update-cubes-fn)
      (renderer-draw-fn))))