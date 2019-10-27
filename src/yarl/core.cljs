(ns yarl.core
  (:require
    ["rot-js" :as rot]))


(defonce game (atom nil))

(reset!
  game
  {:player
    {:x 1
     :y 1
     :symbol "@"}})


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


(defn update-state [state dt event]
  (js/console.log (.-type event))
  (if event
    (handle-input state event)
    state))


(defn update-game [event]
  (prn "game state:" (swap! game update-state nil event)))


(defn addEventListener [event]
  (.addEventListener js/window event #(update-game %)))


(defn register-input-handler []
  (addEventListener "keydown")
  (addEventListener "keyup")
  (addEventListener "keypress"))

(defn render-player [display {:keys [x y symbol] :as player}]
  (doto display
    (.draw x y symbol)))


(defn render [display state]
  (.clear display)
  (render-player display (:player state)))


(defn init []
  (let [display (rot/Display. #js {"fontFamily" "Menlo"})
        el (.getContainer display)]
    (.. js/document -body (appendChild el))
    (letfn [(render-loop []
                (render display @game)
                (js/requestAnimationFrame render-loop))]
      (render-loop))
    (register-input-handler)))


(defn ^:dev/after-load start []
  nil)

(defn ^:export main [] (init))
