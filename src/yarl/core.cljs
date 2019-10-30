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
      (= code (.-VK_K rot/KEYS)) (move-player state [0 -1])
      (= code (.-VK_J rot/KEYS)) (move-player state [0 1])
      (= code (.-VK_H rot/KEYS)) (move-player state [-1 0])
      (= code (.-VK_L rot/KEYS)) (move-player state [1 0])
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
  (addEventListener "keypress"))


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


(defn init []
  (set! (.. rot -Display -Rect -cache) true)
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
