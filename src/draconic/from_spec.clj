(ns draconic.from-spec
  (:require [draconic.fx.ml :as ml]
            [draconic.fx :as fx]
            [draconic.fx.assignment :as fxa]
            [clojure.spec :as s]
            [draconic.macros :as dramac]
            [com.rpl.specter :as sp :refer :all]
            [clojure.string :as str])
  (:import (javafx.scene.control Label)
           (javafx.beans.value ChangeListener)))


(defn get-for-display [thingamabob]
  (-> (cond (keyword? thingamabob) (name thingamabob)
            :default (str thingamabob))
      (str/replace "-" " ")
      (str/capitalize)
      ))

(defn gen-field [labeltext field-name workable-spec actual-spec locstring]
  (let [actual-labeltext (get-for-display labeltext)
        actual-field-name (get-for-display field-name)
        [specNode {:strs [specLabel specControl] :as el-mappo}] (ml/make-node-with-id-map locstring)]
    (println "Thing loaded?")
    ;; Give each element a unique name- Important in case one needs to look up a node by ID.
    (.setId specLabel (str actual-field-name "Label"))
    ;(fxa/try-set! specLabel :id (str field-name "Label"))
    ;(println "things are at least functional")
    (.setId specControl (str actual-field-name "Control"))
    ;(fxa/set!-typed specControl :id (str field-name "Label"))
    ;; Set the label text
    (.setText specLabel actual-labeltext)
    (println "all should be good?")
    [specNode {field-name (into el-mappo
                                {:workable-spec workable-spec
                                 :actual-spec   actual-spec})}]
    ))

(defn var-lookup [symbolico]
  (try
    (var-get (resolve symbolico))
    (catch Exception e
      symbolico)))
(defn print-return [thingy]
  (println thingy)
  thingy)

(defn expand-spec [spec-keywordio]
  (let [initial-form (try
                       (s/form spec-keywordio)
                       (catch Exception e
                         (println "happened! " spec-keywordio)
                         spec-keywordio))]
    (if (coll? initial-form)
      (transform (walker #(not (coll? %)))
                 #(cond
                    (keyword? %)
                    (expand-spec %)
                    :default %)
                 initial-form)
      initial-form)))

(defn explode-spec [expanded-spec]
  (cond
    (coll? expanded-spec) (transform [ALL] explode-spec expanded-spec)
    (symbol? expanded-spec) (let [real-thing (var-lookup expanded-spec)] real-thing)
    :default expanded-spec))

(defn get-real-spec [specco-keywordo]
  (-> specco-keywordo
      (print-return)
      (expand-spec)
      (explode-spec)
      ))

(defn make-spec-workable [el-specco]
  (cond
    (keyword? el-specco) (get-real-spec el-specco)
    :default (explode-spec el-specco)))

(defn ensure-workable-spec-is-wrapped [el-worko]
  (cond
    (coll? el-worko) el-worko
    (fn? el-worko) `(~el-worko)))

(defmulti find-node-for-fn
          "Finds the JavaFX node for a given function. Takes [the-form, the-spec] and dispatches on (first form). The default method
          handles irregular things, like sets"
          (fn [the-form, the-spec] (first the-form)))

(defn make-node-for-spec [el-specco]
  (let [el-worko (-> el-specco
                     (make-spec-workable)
                     (ensure-workable-spec-is-wrapped))]
    (println el-worko " and " el-specco)
    (find-node-for-fn el-worko el-specco)))

(defmulti get-spec-node-data (fn [mappo] (-> mappo (:workable-spec) (first))))
(defn get-data [{:keys [actual-spec workable-spec] :strs [specControl specLabel] :as generated-spec-node-map}]
  (let [tennitive-data (get-spec-node-data generated-spec-node-map)]
    (s/conform actual-spec tennitive-data)))
(defmulti set-spec-node-data! (fn [mappo] (-> mappo (:workable-spec) (first))))
(defn set-data! [{:keys [actual-spec workable-spec] :strs [specControl specLabel] :as generated-spec-node-map} new-data]
  (if (= (s/conform actual-spec new-data) ::s/invalid)
    ::s/invalid
    (set-spec-node-data! generated-spec-node-map new-data)))




(def acceptable-strings
  #{"force" "power" "might" "strength"})

(s/def ::demostring string?)
(s/def ::demointy (s/int-in 0 6))
(s/def ::demomember acceptable-strings)
(s/def ::democombo (s/and ::demostring ::demointy ::demomember))
(s/def ::demomap (s/keys :req [::demostring ::demomember ::demointy]))

(dramac/defmethod-using-map find-node-for-fn
  [the-form, the-spec]
  {string?  (let [[nodio {{:strs [specLabel specControl] :as el-mappo} the-spec} :as initial-node] (gen-field the-spec the-spec the-form the-spec "spec/textfield.fxml")]
              (println "stringer")
              (println specControl)
              (fxa/set-change-listener! specControl "whatever" (fx/change-listener [a b c] (println " old " b " and new " c)))
              initial-node)
   :default (str "nah, " the-form)})

(draconic.fx.ml/launch-fxml-window [#(make-node-for-spec ::demostring)])