(ns draconic.fx.tree
  (:require [draconic.fx :as fx :refer :all]
            [draconic.fx.ml :as fxml :refer :all]
            [clojure.string :as str]
            [draconic.fx.assignment :as fxa]
            [com.rpl.specter :as sp :refer :all]
            [draconic.macros :as dramac]
            [clojure.spec :as s]
            [clojure.java.io :as io])
  (:import (javafx.scene.control TreeItem)
           (com.blakwurm.draconic draconicKt)))

(defn make-simple-tree
  "Builds a root tree-item with children from a structure, taking the same arguments as core/tree-seq plus an optional listener fn of one argument that will be fired whenever a sub-node is selected."
  ([branch? children root] (make-simple-tree branch? children root (fn [e] (println "thing has been selected-> " e))))
  ([branch? children root listen-fn]
   (let [this-item (new TreeItem root)]
     (println "item is " root)

     (when (branch? root)
       (println "has children!")
       (let [the-children (mapv #(make-simple-tree branch? children %) (children root))]
         (println "the-children: " the-children)
         (run! (fn [child-item]
                 (-> this-item
                     (.getChildren)
                     (.add child-item)))
               the-children)
         ))
     this-item))
  )


(defn generate-tree-item-cell-factory [display-fn]
  (draconicKt/makeTreeItemCellFactory display-fn)
  )
(defn set-cell-factory [tree-view display-fn]
  (.setCellFactory tree-view (generate-tree-item-cell-factory display-fn)))

(defn set-selection-listener [tree-view listener-fn]
  (println " should be done? ")
    (draconicKt/addTreeSelectionListener tree-view listener-fn))



(comment (def tree-map (let [tree-thing (make-simple-tree
                                  #(contains? % :children)
                                  :children
                                  '{:display     "Everything"
                                    :select-type [:all :all]
                                    :children    [{:display     "Characters"
                                                   :select-type [:character :all]
                                                   :children    [{:display     "Solars"
                                                                  :select-type [:character :solar]}
                                                                 {:display     "Mortals"
                                                                  :select-type [:character :mortal]}]}
                                                  {:display     "Chrons"
                                                   :select-type [:chron :all]
                                                   :children    [{:display     "Templates"
                                                                  :select-type [:chron :template]}
                                                                 {:display     "Now Playing"
                                                                  :select-type [:chron :current]}]
                                                   }]}
                                  )]
                 (println tree-thing)
                 tree-thing))

  (fxml/defcontroller tree-test-controller
    "Sets up the tree view"
    [mappa testtree]
    (println testtree)
    (println "Hello")
    (.setRoot testtree tree-map)
    (.setShowRoot testtree false)

    (set-cell-factory testtree (fn [thingy] (:display thingy)))
    (set-selection-listener testtree
                            (fn [oldval newval]
                              (println "things are " oldval " and then " newval)))
    )

  (defn launch-tree-test-window []
    (draconic.fx.ml/launch-fxml-window ["treeviewtest.fxml"]
                                       #'tree-test-controller)))