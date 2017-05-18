(ns draconic.fx.ui
  (:require [draconic.ui.atomic-node :as an]
            [draconic.ui :as ui]
            [draconic.fx :as fx])
  (:import (clojure.lang RT)
           (javafx.scene.control TreeItem TextInputControl)
           (javafx.scene Node)))

(defn name-no-load
  "For use when Clojure doesn't want to let you import a class properly. Arg is a symbol/string/keyword/thing who's (name) evals to the name of the desired class"
  [s] (RT/classForNameNonLoading (name s)))

(comment (def text-atom-bindings {:-ui-get-state  (fn [this] (.getText this))
                          :-ui-set-state! (fn [this s] (.setText this s))})
  (run! (fn [classname]
          (extend (name-no-load classname) bind/Atomic-Node text-atom-bindings))
        [:javafx.scene.control.Labeled
         :javafx.scene.control.TextInputControl]))



(extend-protocol an/Atomic-Node
  javafx.scene.Node
  (-ui-get-id [t] (.getId t))
  (-ui-set-id! [t s] (.setId t s))
  javafx.scene.control.Label
  (-ui-get-id [t] (.getId t))
  (-ui-set-id! [t s] (.setId t s))
  (-ui-set-state! [t s] (.setText t s))
  (-ui-get-state [t] (.getText t))
  )