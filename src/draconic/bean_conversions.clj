(ns draconic.bean-conversions
  (:require [com.rpl.specter :refer :all]
            [clojure.reflect :as r]
            [clojure.string :as str]
            [clojure.set :as set]
            [camel-snake-kebab.core :as csk]))

(def readme "This namespace is... unfinished. The goal here is to make it possible to convert a tree of objects into clojure data, and then apply any changes to that data back to the objects in-place. If this is achieved, something akin to react could be fashioned for JavaFX, without altering the API too much.")

(def example-thing (new com.blakwurm.draconic.MutExample "What is this?" 42))
(def nested-example (new com.blakwurm.draconic.parentThingy))

(defn get-methods [thingy]
  "Gets all the methods for an object"
  (let [reflmap (r/reflect thingy)]
    (filter #(instance? clojure.reflect.Method %)
            (:members reflmap))))

(defn name-has [teh-name-hazor thing]
  (str/includes? (str (:name teh-name-hazor)) thing))

(defn get-getters [methseq]
  (filter #(or (name-has % "get")
               (name-has % "is")) methseq))

(defn get-setters [methseq]
  (filter #(name-has % "set") methseq))

(defn get-setters-and-getters [methseq]
  "Given a seq of clojure.reflect.Method, gets only getters and setters"
  (into (get-setters methseq)
        (get-getters methseq)))



(defn ensure-public [gs-seq]
  "Ensures that all the things in a seq of clojure.reflect.Method are public"
  (filter #(contains? (:flags %) :public)
          gs-seq))

(defn boil-down-method [method prefix]
  (-> method
      (:name)
      (str)
      (str/replace prefix "")))

(defn boil-down-methods [seqqie prefix]
  (->> seqqie
       (filter #(name-has % prefix))
       (map #(boil-down-method % prefix))))

(defn get-only-these-methods [seqqie set-o-names prefix]
  (filter (fn [n] (let [thingy (boil-down-method n "set")]
                    (contains? set-o-names thingy))) seqqie))


(defn ensure-paired [gs-pub-seq]
  (let [settorz (into #{} (boil-down-methods gs-pub-seq "set"))
        gettorz (into #{} (boil-down-methods gs-pub-seq "get"))
        izorz (into #{} (boil-down-methods gs-pub-seq "is"))
        what-has-both (set/union settorz (into gettorz izorz))
        good-settorz (get-only-these-methods gs-pub-seq what-has-both "set")
        good-gettorz (get-only-these-methods gs-pub-seq what-has-both "get")
        good-izorz (get-only-these-methods gs-pub-seq what-has-both "is")]
    (if (= (count settorz) (count (into gettorz izorz)))
      gs-pub-seq
      (-> []
          (into good-settorz)
          (into good-gettorz)
          (into good-izorz)))))



(defmacro replaces-with-member-access [method-name]
  (keyword (str "." method-name)))

(defmacro accesses-a-member [method-name doodaad-name]
  (println "params are " method-name " and " doodaad-name)
  (let [return-thing (read-string (str "(." method-name " " doodaad-name ")"))]
    (println "return thing is " return-thing)
    return-thing))

(defmacro access-member [method-symbol doodad-name]
  '(~(symbol (str "." ~method-symbol)) ~doodad-name))

(defmacro make-java-access-symbol [namo]
  (symbol (str "." namo)))

(defn is-scalar? [thingy]
  (or (number? thingy)
      (string? thingy)
      (true? thingy)
      (false? thingy)
      (keyword? thingy)
      (symbol? thingy)
      ))

(declare get-map-of-thing set!-from-map)

(defn make-member-access-function
  ([method-symbol] (make-member-access-function method-symbol nil))
  ([method-symbol optional-thingy]
   (let [function-param (gensym "objecto")
         member-access-symbol (symbol (str "." method-symbol))
         to-return (if optional-thingy
                     `(fn [~function-param] (~member-access-symbol ~function-param ~optional-thingy))
                     `(fn [~function-param] (~member-access-symbol ~function-param)))]
     (println to-return)
     (eval to-return)))
  )
(defn extract-from-method [thing-a-ma-bob methodo]
  (let [method-name-string (csk/->kebab-case (boil-down-method methodo "get"))
        method-call-keyword (:name methodo)
        actual-method (make-member-access-function method-call-keyword)
        ;accessed-thing (access-member method-call-keyword thing-a-ma-bob)
        thing-what-you-got (actual-method thing-a-ma-bob)
        thing-what-gets-returned (if (is-scalar? thing-what-you-got)
                                   thing-what-you-got
                                   (get-map-of-thing thing-what-you-got))
        ]
    [(keyword method-name-string)
     thing-what-gets-returned]))



(defn get-bean-methods [thing-a-ma-bob]
  (-> thing-a-ma-bob
      (get-methods)
      (get-setters-and-getters)
      (ensure-public)
      (ensure-paired)))



(defn get-map-of-thing [thing-a-ma-bob]
  (let [actual-methods (get-bean-methods thing-a-ma-bob)
        gettorz-and-izorz (get-getters actual-methods)]
    (into {} (map (fn [methodo]
                    (let [method-name-string (str/lower-case (boil-down-method methodo "get"))]
                      (extract-from-method thing-a-ma-bob methodo)))
                  gettorz-and-izorz))))

(defn make-access-symbol-from-keyword [teh-keyword prefix]
  (let [string-before-symbol (csk/->camelCase (str/replace (str (str/replace (str teh-keyword) ":" (str prefix "-"))) "is-" ""))]
    (symbol string-before-symbol)))

(defn set!-from-map [thing-a-ma-bob mappo]
  (let [actual-methods (get-bean-methods thing-a-ma-bob)
        settorz (get-setters actual-methods)]
    (map (fn [[field-key field-prop]]
           (if (is-scalar? field-prop)
             (let [field-setter-symbol (make-access-symbol-from-keyword field-key "set")
                  member-access-fn (make-member-access-function field-setter-symbol field-prop)]
              (member-access-fn thing-a-ma-bob))
             (let [get-method (make-member-access-function (make-access-symbol-from-keyword field-key "get"))
                   thing-what-get-gotten (get-method thing-a-ma-bob)]
               (set!-from-map thing-what-get-gotten field-prop))))
         mappo)))

