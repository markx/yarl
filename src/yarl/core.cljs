(ns yarl.core
  (:require
    ["rot-js" :as rot]
    [yarl.world :as world]))


(defonce game (atom nil))

(def WORLD-SIZE [80 40])

(comment
  (reset! game (world/make-world WORLD-SIZE))
  (swap! game update :world world/smooth-world))

(defn move-player [state [dx dy]]
  (-> state
    (update-in [:player :x] + dx)
    (update-in [:player :y] + dy)))


(defn handle-input [state event]
  (let [code (.-keyCode event)]
    (cond
      (= (.-type event) "keyup") state ;; don't move
      (= code (.-VK_UP rot/KEYS)) (move-player state [0 -1])
      (= code (.-VK_DOWN rot/KEYS)) (move-player state [0 1])
      (= code (.-VK_LEFT rot/KEYS)) (move-player state [-1 0])
      (= code (.-VK_RIGHT rot/KEYS)) (move-player state [1 0])
      :else state)))


(defn update-state [state event]
  (if event
    (handle-input state event)
    state))


(defn update-game [event]
  (let [old @game]
    (compare-and-set! game old (update-state old event))))


(defn addEventListener [event]
  (.addEventListener js/window event #(update-game %)))


(defn register-input-handler []
  (addEventListener "keydown")
  (addEventListener "keyup")
  (addEventListener "keypress"))


(defn render-player [display {:keys [x y glyph] :as player}]
  (doto display
    (.draw x y glyph)))


(defn render-map [display m]
  (doseq [tile (flatten m)]
    (let [[row col] (:pos tile)]
      (.draw display col row (:glyph tile)))))


(defn render [display state]
  (render-map display (:world state))
  (render-player display (:player state)))


(defn init []
  (let [display (rot/Display. #js {"fontFamily" "Menlo"
                                   "width" (first WORLD-SIZE)
                                   "height" (second WORLD-SIZE)})
        el (.getContainer display)]
    (.. js/document -body (appendChild el))
    (reset! game (world/make-world WORLD-SIZE))
    (letfn [(render-loop []
                (render display @game)
                (js/requestAnimationFrame render-loop))]
      (render-loop))
    (register-input-handler)))


(defn ^:dev/after-load start []
  nil)

(defn ^:export main [] (init))