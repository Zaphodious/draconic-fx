(ns draconic.launch
  (:require
    [draconic.fx :as fx :refer [run-now run-later]]
    [draconic.fx.ml :as fxml]
    [clojure.string :as str]))

(defn start-app
  "Starts a JavaFX App. Takes a var or the name of a var."
  [launch-function]
  (let [{:strs [stage] :as the-map} (run-now (launch-function))]
    (.setOnCloseRequest stage (fx/event-handler [evento] (println "Close Things!")))
    the-map))