(ns nqueens.core
  (:require-macros 
    [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [clojure.string :as str]
            [om.dom :as dom :include-macros true]
            [goog.dom :as goog-dom]
            [goog.events :as events]
            [cljs.core.async :as async :refer [put! chan <! >! timeout]]))

(enable-console-print!)



(defn safe? [col row placements] 
  (let [positions (map-indexed (fn [x y] [x y]) placements)
        not-in-row (fn [[x y]] (not= y row))
        not-in-diag-1 (fn [[x y]] (not= (- y row) (- x col)))
        not-in-diag-2 (fn [[x y]] (not= (- y row) (- col x)))
        ]
    (every? (every-pred not-in-row not-in-diag-1 not-in-diag-2) positions)))

(defn n-queens-seq [size]
  (letfn [(search [n]
            (if (< n 0) [[]]
                (for [arrangement (search (- n 1))
                      row (range 0 size ) 
                      :when (safe? n row arrangement)]
                  (conj arrangement row))))]
    (search (- size 1))))


(def queen "♛")

(defn draw-row [rowId positions]
  (apply dom/tr nil
         (map #(dom/td nil (if (= rowId %) queen nil)) positions)))


(defn style [& styles]
  (dom/style nil (str/join \newline styles)))


(def board-style
  (style
   ".chess_board tr:nth-child(even) td:nth-child(odd),
         .chess_board tr:nth-child(odd) td:nth-child(even) { 
            background: chocolate; 
            box-shadow: inset 0 0 10px rgba(0,0,0,.4);
            -moz-box-shadow: inset 0 0 10px rgba(0,0,0,.4);
            -webkit-box-shadow: inset 0 0 10px rgba(0,0,0,.4);
          }
          .chess_board td { 
            background: BurlyWood;
            width: 40px; height: 40px; 
            font-size: 20px; 
            color: black; 
            text-align: center;
          }") )

(defn draw-table [pos]
(dom/table
    #js {:className "chess_board"}
    board-style
 (apply 
    dom/tbody nil

    (let [size (count pos)] 
      (do
        (map #(draw-row % pos) (range size))))))  )

(defn board [state owner]
  (reify
    om/IRender
    (render [this]    
      (dom/div nil
               (draw-table state)
               (dom/button nil "next")))))

(def positions
  (atom  (vec (repeat 8 nil))))

(defn show-board! [q-positions]
  (reset! positions q-positions))

(defn slideshow []
  (go
    (doseq [queens (n-queens-seq 10 )]
     (do
       (show-board! queens)
       (<! (timeout 1500))))))

(om/root board  positions
         {:target (goog-dom/getElement "chessboard" )})

(set! (.-onload js/window) slideshow)

