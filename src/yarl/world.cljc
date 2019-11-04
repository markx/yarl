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
  (vec
    (for [[row tile-row] (map-indexed list world)]
      (mapv
        (fn [tile col]
          (assoc tile :pos [col row]))
        tile-row (range)))))


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


(defn create-wall [width height]
  (->> (TILES :wall)
    (repeat width)
    (vec)
    (repeat height)
    (vec)))


(defn update-cell [cells index tile-type]
  (update-in cells index #(merge % (TILES tile-type))))


(defn dig-room [cells {:keys [x y width height]}]
  (let [indexes (for [i (range (inc y) (dec (+ y height)))
                      j (range (inc x) (dec (+ x width)))]
                  [i j])]
    (reduce #(update-cell %1 %2 :floor) cells indexes)))


(defn random-room [width height minSize maxSize]
  (let [w (+ minSize (rand-int (- maxSize minSize)))
        h (+ minSize (rand-int (- maxSize minSize)))
        x (+ 1 (rand-int (- width w 2)))
        y (+ 1 (rand-int (- height h 2)))]
    {:width w
     :height h
     :x x
     :y y}))


(defn intersect? [a b]
  (and
    (<= (:y a) (+ (:height b) (:y b)))
    (>= (+ (:height a) (:y a)) (:y b))
    (<= (:x a) (+ (:width b) (:x b)))
    (>= (+ (:width b) (:x b)) (:x a))))


(defn horizontal-passage [cells x1 x2 y]
  (let [start (min x1 x2)
        end   (max x1 x2)]
    (reduce #(update-cell %1 [y %2] :floor)
            cells
            (range start (inc end)))))


(defn vertical-passage [cells y1 y2 x]
  (let [start (min y1 y2)
        end   (max y1 y2)]
    (reduce #(update-cell %1 [%2 x] :floor)
            cells
            (range start (inc end)))))


(defn rect-center [{:keys [x y width height]}]
  {:x (+ x (Math/round (/ width 2)))
   :y (+ y (Math/round (/ height 2)))})


(defn passage [cells a b]
  (let [[a b] (shuffle [a b])
        {x1 :x  y1 :y } (rect-center a)
        {x2 :x  y2 :y } (rect-center b)]
    (-> cells
        (horizontal-passage x1 x2 y1)
        (vertical-passage y1 y2 x2))))


(defn generate-dungeon [width height {:keys [room-count max-size min-size]
                                      :or {room-count 30 max-size 16 min-size 6}}]
  (let [wall (tiles-indexed (create-wall width height))
        rooms (repeatedly room-count #(random-room width height min-size max-size))
        valid-rooms (reduce (fn [result room]
                              (if (some #(intersect? room %) result)
                                result
                                (conj result room)))
                            nil
                            rooms)
        room-pairs  (map vector valid-rooms (next valid-rooms))]
    (as-> wall %
      (reduce dig-room % valid-rooms)
      (reduce (fn [cells [a b]] (passage cells a b))
              %
              room-pairs)
      [% (rect-center (first valid-rooms))])))


(defn create-player [{:keys [x y]}]
  {:x x
   :y y
   :glyph "@"})


(defn create [[width height]]
  (let [[dungeon start-pos] (generate-dungeon width height nil)]
    {:player (create-player start-pos)
     :map dungeon
     :width width
     :height height}))


