(ns yarl.world)



(def TILES
  {:floor {:type :floor
           :blocking false
           :opaque false
           :glyph "."}

   :wall {:type :wall
          :blocking true
          :opaque true
          :glyph "#"}

   :bound {:type :bound
           :blocking true
           :opaque true
           :glyph "X"}})


(defn tiles-indexed [world]
  (for [[row tile-row] (map-indexed list world)]
    (vec (map #(assoc %1 :pos [row %2]) tile-row (range)))))


(defn generate-map [width height]
    (letfn [(random-tile []
              (TILES (rand-nth [:floor :wall])))
            (random-row []
              (vec (repeatedly width random-tile)))]
      (vec (tiles-indexed (repeatedly height random-row)))))


(defn- distance [[x1 y1] [x2 y2]]
  (Math/sqrt (+
               (Math/pow (- x1 x2) 2)
               (Math/pow (- y1 y2) 2))))

(defn- is-around [a b]
  (< (distance (:pos a) (:pos b)) 2))

(defn- around-blocks [t tiles]
  (filter #(is-around t %) tiles))


(defn smooth-tile [t tiles]
  (let [block (around-blocks t tiles)
        walls (filter #(= :wall (:type %)) block)]
    (cond
      (>= (count walls) 5) (into t (TILES :wall))
      :else (into t (TILES :floor)))))

(defn smooth-world [world]
  (let [tiles (flatten world)]
    (for [row world]
      (vec (map #(smooth-tile % tiles) row)))))


(defn generate-dungeon [width height]
    (letfn [(random-tile []
              (TILES (rand-nth [:floor :wall])))
            (random-row []
              (vec (repeatedly width random-tile)))]
      (vec (repeatedly height random-row))))


(defn make-player []
  {:x 1
   :y 1
   :glyph "@"})


(defn make-world [[width height]]
  {:player (make-player)
   :world (generate-map width height)
   :width width
   :height height})


