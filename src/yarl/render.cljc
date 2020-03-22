(ns yarl.render)


(defn render-player [display {:keys [x y glyph] :as player}]
  (doto display
    (.draw x y glyph)))


(defn render-map [display m]
  (doseq [row m
          tile row]
    (let [[x y] (:pos tile)
          visible (:visible tile)
          color (if visible "#ddd" "#555")]
      (.draw display x y (:glyph tile) color))))


(defn render [display state]
  (render-map display (:map state))
  (render-player display (:player state)))


