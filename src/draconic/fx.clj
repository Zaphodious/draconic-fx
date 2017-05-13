(ns draconic.fx
  (:require [clojure.string :as str])
  (:import (javafx.application Platform)
           (javafx.embed.swing JFXPanel)
           (javafx.event Event EventHandler)
           (javafx.beans.value ChangeListener)
           (com.blakwurm.draconic draconicKt)
           (clojure.lang Named)
           (javafx.beans.property Property)))

(defonce force-toolkit-init (JFXPanel.))
(defonce set-exit-false (Platform/setImplicitExit false))

(def name-of-javafx-thread "JavaFX Application Thread")
(defn run-later*
  [f]
  (if (= name-of-javafx-thread (-> (Thread/currentThread) (.getName)))
    (f)
    (Platform/runLater f)))


(defmacro run-later
  [& body]
  `(run-later* (fn [] ~@body)))

(defn run-now*
  [f]
  (let [result (promise)]
    (run-later
      (deliver result (try (f) (catch Throwable e {:message "An error occured on the JavaFX Thread after using run-now."
                                                   :function f
                                                   :error e}))))
    @result))

(defmacro run-now
  [& body]
  `(run-now* (fn [] ~@body)))

(defn make-event-handler* [funny-boy]
  (reify EventHandler
    (handle [this event]
      (funny-boy event))))

(defmacro event-handler [argvec form]
  `(make-event-handler* (fn ~argvec ~form)))

(defn make-change-listener* [funny-boy]
  (reify ChangeListener
    (changed [this obval oldval newval]
      (funny-boy obval oldval newval))))

(defmacro change-listener [argvec form]
  `(make-change-listener* (fn ~argvec ~form)))

(defn set-change-listener! [node ^Named property-name ^ChangeListener listener]
  (run-now
    (let [prop-get-fn (-> (str "(fn [nodio] (." (name property-name) "Property nodio))")
                          (read-string)
                          (eval))
          ^Property prop (prop-get-fn node)]
      (.addListener prop listener)
      node)))
