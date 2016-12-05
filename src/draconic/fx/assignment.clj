(ns draconic.fx.assignment
  (:require [clojure.string :as str]
            [draconic.fx :as fx]
            [draconic.macros :as dramac]))
(defn class-as-string [object]
  (second (str/split (str (type object)) #"class ")))

(dramac/defmulti-using-map get-defaults
  "Generic getting for UI nodes"
  [node op-id] op-id
  {:default :not-found
   :text (.getText node)
   :children (.getContent node)})
(dramac/defmulti-using-map get-typed
  "Type-spcific getting for UI nodes"
  [node op-id] [op-id (class-as-string node)]
  {:default                               (get-defaults op-id node)
   [:children "javafx.scene.layout.HBox"] (.getChildren node)
   [:children "javafx.scene.layout.VBox"] (.getChildren node)})


(dramac/defmulti-using-map set!-defaults
  "Generic setting for UI nodes"
  [node op-id new-state]
  op-id
  {:default  :not-found
   :text  (.setText node)
   :children (fx/run-now (.setContent node new-state))})
(dramac/defmulti-using-map set!-typed
  "Type-spcific getting for UI nodes"
  [node op-id new-state]
  [op-id (class-as-string node)]
  {:default                               (set!-defaults op-id node new-state)
   [:children "javafx.scene.layout.HBox"] (.addAll (.getChildren node) new-state)
   [:children "javafx.scene.layout.VBox"] (.addAll (.getChildren node) new-state)})






(defn try-get "Gets a bit of state from a UI node"
  [node op-id]
  (get-typed op-id node))

(defn try-set! "Sets a bit of state in a UI node. If the current state and the new state are identical
according to =, returns false."
  [node op-id new-state]
  (let [orig-state (try-get node op-id)
        identical-state? (and (not (#{:not-found} orig-state)) (= orig-state new-state))]
    (if identical-state?
      false
      (set!-typed node op-id new-state))))

(defn try-swap!
  "Swaps a bit of state in a UI node, using a provided function."
  [node op-id swap-fn]
  (set!-typed node op-id (swap-fn (get-typed node op-id))))