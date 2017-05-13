(ns draconic.fx.assignment
  (:require [clojure.string :as str]
            [draconic.fx :as fx]
            [draconic.macros :as dramac])
  (:import (com.blakwurm.draconic draconicKt)
           (javafx.util Callback)))



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
  {:text     (.setText node)
   :id       (do (println "id setting for " node)
                 (.setId node new-state))
   :children (fx/run-now (do (println "set!-default children " node new-state)
                             (.setContent node new-state)))})
(defn- set-default-ingvar [blah oop staaa]
  (println "it actually called default!" (get-method set!-defaults blah))
  (set!-defaults blah oop staaa))


(defn set-change-listener! [node property-name listener]
  ;0
  (-> (str "(fx/run-now
    (println \" should be done? \")
    (.. node

    (" property-name "Property)
    (addListener listener)
        ))")
      (read-string)
      (eval)))


(comment
  (draconicKt/addChangeListener (.getTextProperty node) listener)) ;



(defn set-id! [node id]
  (fx/run-now (-> (.idProperty node))
       (.set id)))
(defn set-content-for-children-havers
  [node children]
  (fx/run-now (.addAll (.getChildren node) children)))
(defn set-content-for-content-havers
  [node content]
  (fx/run-now
    (try
      (.setContent node (if (vector? content)
                          (first content)
                          content))
      (catch Exception e
        (println "exception! " e)))))

(dramac/defmulti-using-map set!-typed
  "Type-spcific getting for UI nodes"
  [node op-id new-state]
  [op-id (class-as-string node)]
  {:default                                      (dramac/dbg (do (println "going to default on this one! " op-id node new-state)
                                                                 (dramac/dbg (set-default-ingvar op-id node new-state))))
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

