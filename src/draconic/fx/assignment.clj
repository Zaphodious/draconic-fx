(ns draconic.fx.assignment
  (:require [clojure.string :as str]
            [draconic.fx :as fx]
            [draconic.macros :as dramac]))
(defn class-as-string [object]
  (second (str/split (str (type object)) #"class ")))

(dramac/defmulti-using-map get-defaults
  "Generic getting for UI nodes"
  [node op-id] op-id
  {:default  :not-found
   :text     (.getText node)
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
  {:text     (println "Huh... setting text!" (.setText node))
   :id       (-> (.idProperty node)
                 (.set new-state))
   :children (fx/run-now (do (println "set!-default children " node new-state)
                             (.setContent node new-state)))})

(defn set-id! [node id]
  (fx/run-now (-> (.idProperty node)
       (.set id))))
(defn set-content-for-children-havers
  [node children]
  (fx/run-now (.addAll (.getChildren node) children)))
(defn set-content-for-content-havers
  [node content]
  (fx/run-now (.set (.contentProperty node) (if (vector? content)
                                              (first content)
                                              content))))

(dramac/defmulti-using-map set!-typed
  "Type-spcific getting for UI nodes"
  [node op-id new-state]
  [op-id (class-as-string node)]
  {:default                                      (dramac/dbg (do (println "going to default on this one! " op-id node new-state)
                                                     (set!-defaults op-id node new-state)))
   [:children "javafx.scene.control.TitledPane"] (fx/run-now (.setContent node new-state))
   [:children "javafx.scene.layout.FlowPane"]    (do (println "called the right one")
                                                     (set-content-for-children-havers node new-state))
   [:children "javafx.scene.control.ScrollPane"] (do (println "setting on a scrollpane " new-state)
                                                     (set-content-for-content-havers node new-state))
   [:children "javafx.scene.layout.HBox"]        (set-content-for-children-havers node new-state)
   [:children "javafx.scene.layout.VBox"]        (set-content-for-children-havers node new-state)})






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