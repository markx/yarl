(ns yarl.render)


(defn render-player [display {:keys [x y glyph] :as player}]
  (doto display
    (.draw x y glyph)))


(defn render-map [display m]
  (doseq [row m
          tile row]
    (let [[r c] (:pos tile)]
      (.draw display c r (:glyph tile)))))


(defn render [display state]
  (render-map display (:world state))
  (render-player display (:player state)))


