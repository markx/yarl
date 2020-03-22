(ns yarl.fov
  (:require
    ["rot-js" :as rot]))


(defn light-passes[map, x, y]
  (not (:opaque (get-in map [y x]))))


(defn update-visibility [map-atom x y r visibility]
  (swap! map-atom assoc-in [y x :visible] (= 1 visibility)))


(defn clear-visibility [m]
  (vec
    (for [row m]
      (mapv
        (fn [tile]
          (assoc tile :visible false))
        row))))


(defn update-fov [{:keys [map player] :as world}]
  (let [input-cb (partial light-passes map)
        checker (rot/FOV.PreciseShadowcasting. input-cb)
        map-copy (atom (clear-visibility map))
        output-cb (partial update-visibility map-copy)
        R (:vision player)]

    (.compute checker (:x player) (:y player) R output-cb)
    (assoc world :map @map-copy)))
