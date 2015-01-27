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
        not-in-diag (fn [[x y]] (every? #(not= (- y row) %) [(- x col) (- col x)]))]
    (every? (every-pred not-in-row not-in-diag) positions)))


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
            width: 20px; height: 20px; 
            font-size: 15px; 
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
    om/IWillMount
    (will-mount [_]
      (go (loop []
            (<! (timeout 1500))

            (om/transact! state 
                          (fn [s] (if-let [next (seq (rest s))] 
                                    next
                                    s)))
              (recur ))))
    om/IRender
    (render [this]    
      (dom/div nil
               (draw-table (first state))
               (dom/button nil "next")))))


(defn boards-view [app owner]
  (reify 
    om/IInitState
    (init-state [_]
      {:add (chan)})
    om/IWillMount
    (will-mount [_]
      (let [add-chan (om/get-state owner :add)]
        (go (loop [] 
              (let [board (<! add-chan)]
                (om/transact! app :boards
                              (fn [bs] (conj bs board))))
              (recur)))))
    om/IRenderState
    (render-state [this {:keys [add]}]
      (dom/div nil 
               (dom/h2 nil "All Boards")
               (dom/li nil 
                       (dom/span nil "add")
                       (dom/button #js {:onClick (fn [e] (put! add (seq (n-queens-seq 8))))} "add board"))
               (apply dom/ul nil
                      (om/build-all board (:boards app)))))))



(def solutions [(seq (n-queens-seq 8 )) (seq (n-queens-seq 7)) ])
(def positions
  (atom  {:boards solutions}))


(om/root boards-view  positions
         {:target (goog-dom/getElement "chessboard" )})





