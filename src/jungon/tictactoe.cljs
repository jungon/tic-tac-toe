(ns ^:figwheel-hooks jungon.tictactoe
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(def board-size 3)

(def win-length 3)

(defn new-board [n]
  (vec (repeat n (vec (repeat n "B")))))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (atom {:text "Welcome to tic tac toe"
         :board (new-board board-size)
         :game-status :in-progress}))

(defn computer-move [board]
  (let [remaining-spots (for [i (range board-size)
                              j (range board-size)
                              :when (= (get-in board [j i]) "B")]
                          [j i])
        move (when (seq remaining-spots)
               (rand-nth remaining-spots))]
    (if move
      (assoc-in board move "C")
      board)))

(defn straight [owner board [x y] [dx dy] n]
  (every? true?
          (for [i (range n)]
            (= (get-in board [(+ (* dx i) x)
                              (+ (* dy i) y)])
               owner))))

(defn win? [owner board n]
  (some true?
        (for [i (range board-size)
              j (range board-size)
              dir [[1 0] [0 1] [1 1] [1 -1]]]
          (straight owner board [i j] dir n))))

(defn full? [board]
  (every? #{"P" "C"} (apply concat board)))

(defn game-status [board]
  (cond
    (win? "P" board board-size) :player-victory
    (win? "C" board board-size) :computer-victory
    (full? board) :draw
    :else :in-progress))

(defn update-status [state]
  (assoc state :game-status (game-status (:board state))))

(defn check-game-status [state]
  (-> state
      (update-in [:board] computer-move)
      (update-status)))

(defn blank [i j]
  [:rect {:key (str i "-" j)
          :width 0.9
          :height 0.9
          :x i
          :y j
          :fill "grey"
          :on-click
          (fn rect-click []
            (when (= (:game-status @app-state) :in-progress)
              (swap! app-state assoc-in [:board j i] "P")
              (if (win? "P" (:board @app-state) win-length)
                (swap! app-state assoc :game-status :player-vicotory)
                (swap! app-state check-game-status))))}])

(defn circle [i j]
  [:circle
   {:r 0.35
    :stroke "green"
    :stroke-width 0.12
    :fill "none"
    :cx (+ 0.5 i)
    :cy (+ 0.5 j)
    }])

(defn cross [i j]
  [:g {:stroke "darkred"
       :stroke-width 0.4
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.5 i) "," (+ 0.5 j) ") "
            "scale(0.3)")}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 1 :y1 -1 :x2 -1 :y2 1}]])

(defn app []
  [:center
   [:h1 (:text @app-state)]
   [:h2
    (case (:game-status @app-state)
      :player-vicotory "You won!"
      :computer-victory "Computer wins."
      :draw "Draw."
      "")
    [:button
     {:on-click
      (fn new-game-click [e]
        (swap! app-state assoc
               :board (new-board board-size)
               :game-status :in-progress))}
     "New Game"]]
   (into
    [:svg {:view-box (str "0 0 " board-size " " board-size)
           :width 700
           :height 700}]
    (for [i (range board-size)
          j (range board-size)]
      (case (get-in @app-state [:board j i])
        "B" [blank i j]
        "P" [circle i j]
        "C" [cross i j])))])

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rdom/render [app] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^:after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  )
