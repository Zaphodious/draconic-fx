(ns draconic.fx
  (:import (javafx.application Platform)
           (javafx.embed.swing JFXPanel)
           (javafx.event Event EventHandler)))

(defonce force-toolkit-init (JFXPanel.))
(defonce set-exit-false (javafx.application.Platform/setImplicitExit false))

(defn run-later*
  [f]
  (Platform/runLater f))

(defmacro run-later
  [& body]
  `(run-later* (fn [] ~@body)))

(defn run-now*
  [f]
  (let [result (promise)]
    (run-later
      (deliver result (try (f) (catch Throwable e e))))
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