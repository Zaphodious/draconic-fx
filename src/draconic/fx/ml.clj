(ns draconic.fx.ml
  (:require [draconic.fx :as fx :refer :all]
            [clojure.string :as str]
            [draconic.fx.assignment :as fxa]
            [com.rpl.specter :as sp :refer :all])
  (:import (javafx.scene Scene)
           (javafx.stage Stage)
           (java.io ByteArrayInputStream)
           (javafx.fxml FXMLLoader)
           (javafx.scene.control Alert Alert$AlertType)))

(defn make-loader
  "Makes and returns an FXML Loader"
  []
  (new FXMLLoader))

(defn make-node-with-id-map
  "Returns a vector of the parent FX node from FXML file at the location specified, and a map of String->Node for each item in the FXML file with an ID."
  [locstring]
  (let [fxml-string (slurp locstring)
        loader (make-loader)
        node (run-now (.load loader (new ByteArrayInputStream (.getBytes fxml-string))))
        map-of-IDs (into {} (.getNamespace loader))]
    [node map-of-IDs]))

(defn make-node-from [string-or-fn]
  (cond
    (string? string-or-fn) (make-node-with-id-map string-or-fn)
    (fn? string-or-fn) (string-or-fn)
    :default (throw (new Exception (str string-or-fn " is neither a string or an IFn, and thus we can't make a node from it.")))))

(defn make-composite-nodes
  "Makes a composite node using multiple FXML files. Takes a vector containing the first FXML file to load, followed by an arbitrary number of vectors. The first element of each vector is the ID where the resulting node will end up, and the second element is either a string representing an FXML file location or a zero-arg fn yielding a vector of [generated node, map of String->Node]"
  [[first-loc & pairs-of-id-to-loc]]
  (println pairs-of-id-to-loc)
  (if (empty? pairs-of-id-to-loc)
    (make-node-with-id-map first-loc)
    (let [[primary-node __first-map] (make-node-with-id-map first-loc)
          finished-map (reduce (fn [o-mappo-grande [this-str-id node-loc]]
                                 (let [[o-nodio o-mappo-novo] (make-node-with-id-map node-loc)
                                       mappo-grande-novo (into o-mappo-grande o-mappo-novo)]
                                   (println "nodio " o-nodio "e o mappo grande novo" mappo-grande-novo)
                                   (fxa/try-set! (get o-mappo-grande this-str-id) :children [o-nodio])
                                   (println "add-children isn't the issue?")
                                   mappo-grande-novo))
                               __first-map
                               pairs-of-id-to-loc)]
      [primary-node finished-map]
      )))

;; 8776048833
;; 978.38, 6359825

(defn make-stage
  "Puts a node into a JavaFX Stage"
  [the-node]
  (run-now
    (let [stage (new Stage)
          scene (new Scene the-node)]
      (.setScene stage scene)
      stage
      )))

(defn show-stage
  [the-stage]
  (run-now (do (.show the-stage)
               the-stage)))



(defn launch-fxml-window
  "Launches a window. First argument is a vector passed to make-composite-nodes. Second is a controller fn
  used to setup the resulting window post-launch (this function returns whatever this one does), defaults to a function
   n->n. Third is a function that converts the a node into a stage (All post-composit loading and styling should be done here),
   defaults to draconic.fx.ml/make-stage, resulting in a functional if boring stage.

   As this operation is stateful, please note that the operation order is 1) initialization/composition 2) passing the resulting node through the stage-making function and then showing it 3) passing the map through the controller-builder"
  ([locstrings] (launch-fxml-window locstrings (fn [m] m) make-stage))
  ([locstrings controller-fn] (launch-fxml-window locstrings controller-fn make-stage))
  ([locstrings controller-fn node->stage]
   (let [[the-node the-map] (make-composite-nodes locstrings)
         shown-stage (-> the-node
                         (node->stage)
                         (show-stage))]

     (run-now (controller-fn (into the-map {"stage" shown-stage}))))))

(comment "The following is an example for how to launch a window using multiple FXML files, attaching a 'controller'-like function that sets the app's initial state.")
(comment (launch-fxml-window ["resources/containerui.fxml"
                              ["midbox" "resources/simpleui.fxml"]
                              ["toppane" "resources/simplebuttonbar.fxml"]]
                             (fn [{:strs [doesNothingButton doesSomethingButton aButton aLabel stage]
                                   :as   mappo-of-named-elements}]
                               (println "The function has been called. " aButton)
                               (.setOnAction aButton (event-handler
                                               [event]
                                               (let [the-alert (new Alert (Alert$AlertType/INFORMATION))]
                                                   (.setTitle the-alert "You pressed a button!")
                                                   (.setHeaderText the-alert "Button Pressing Message!")
                                                   (.setContentText the-alert "Horray! The Controller Fn Works!")
                                                   (.showAndWait the-alert)
                                                   (println the-alert))))
                               mappo-of-named-elements)))

;;;Alert alert = new Alert(AlertType.INFORMATION);
;;;alert.setTitle("Information Dialog");
;;;alert.setHeaderText("Look, an Information Dialog");
;;;alert.setContentText("I have a great message for you!");

;;;alert.showAndWait();
;;;