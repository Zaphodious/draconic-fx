(ns draconic.fx.parent
  (:require [draconic.fx :as fx]
            [clojure.string :as str]))

(defmulti add-children
          "Adds children nodes to a parent node. Multimethod because dispatch on stringified classname is required, as FX classes can't be initialized outside of the FX thread."
          (fn [parent-node & children] (str/replace (str (type parent-node)) "class " "")))

(defmethod add-children :default
  [parent-node & children]
  (fx/run-now (.setContent parent-node (first children)))
  parent-node)

(defmethod add-children "javafx.scene.layout.VBox"
  [parent-node & children]
  (.addAll (.getChildren parent-node) children))

(defmethod add-children "javafx.scene.layout.HBox"
  [parent-node & children]
  (.addAll (.getChildren parent-node) children))

