(ns draconic.fx.cells
  (:require [draconic.fx :as fx]
            [clojure.string :as str])
  (:import (javafx.scene.control Cell Labeled)
           (javafx.util Callback)))
(defn print-return [thingy] (do (println thingy) thingy))

(defn this-be-called [is-actually-called]
  (println "this was called! proof: " is-actually-called))

;; Good idea, but buggy implimentation. Will revisit this later.
(comment (defn gen-cell-constructor
          "Generates a constructor function for a cell, to be used with a ListView and others like it.

   Text-fn and graphic-fn are each two-arg fns from ((new item to display) (is the cell empty)) -> (String/Node, respectfully), and the result will be applied to the cell's respective properties. As the updateItem method itself has necessary side effects, please note that the call order is (text-fn) and then (graphic-fn) sequentially."
          [cell-type & {:keys [text-fn graphic-fn properties]}]
          (let [classname (if (class? cell-type)
                            (-> cell-type (str) (str/replace "class " "") (symbol))
                            (-> cell-type (name) (symbol)))
                update-item-symbol (symbol "updateItem")
                newthingsym (gensym "newthing")
                isemptysym (gensym "isempty")
                prop-supah (conj (map (fn [[symo argy]] `(~'proxy-super ~(symbol (name symo)) ~argy)) properties) `do)]

     ;(println the-proxy-ast)
            (fn []
              (eval

                `(proxy [~classname] []
                   (~update-item-symbol
                     [~newthingsym ~isemptysym]
                     (let [~'new-text (~text-fn ~newthingsym ~isemptysym)
                           ~'new-graphic (~graphic-fn ~newthingsym ~isemptysym)]
                       (println "made it this far")
                       ~prop-supah
                       (println "actually calling the update")
                       (proxy-super ~update-item-symbol ~newthingsym ~isemptysym)
                       (println "made it past the super call")
                       (proxy-super setText ~'new-text)
                       (println "set the text?")
                       (when ~'new-graphic (proxy-super setGraphic ~'new-graphic))
                       (println "set the graphic!")))))))))




(defn set-cell-factory [thing-with-cell-factory cell-constructor-fn]
 (let [proxycallback (proxy [Callback] []
                       (call [argument] (cell-constructor-fn)))]
   (do (println "setting the factory")
       (.setCellFactory
         thing-with-cell-factory
         proxycallback)
       (println "have set the cell factory"))

   proxycallback))

(defn returns-nil[a b] nil)