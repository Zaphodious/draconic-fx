(ns draconic.ui.fx
  (:require [draconic.ui.atomic-node :as an :refer [Atomic-Node]]
            [draconic.fx :as fx]
            [draconic.ui :as ui]
            [clojure.spec :as s]
            [clojure.data :as data :refer [diff]]
            [clojure.core.async :as as
             :refer [chan go go-loop >! <! >!! <!! pub sub]]
            [draconic.fx.ml :as fxml]
            [draconic.fx.cells :as cells])
  (:import (javafx.scene.control TextField Label CheckBox ListView ListCell ComboBox ToggleGroup RadioButton Toggle Labeled ToggleButton TextInputControl TextArea TabPane Tab ScrollPane)
           (javafx.scene Node)
           (clojure.lang IPersistentMap Named)
           (javafx.beans.property Property)
           (javax.swing.event ChangeListener)
           (javafx.util Callback)
           (javafx.scene.text TextFlow)
           (org.controlsfx.control Rating)))
(s/def ::any any?)
(reset! ui/new-chan-fn #(as/chan (as/sliding-buffer 1)))
(extend-protocol clojure.core.async.impl.protocols/Channel
  nil
  (close! [it] "tried to close a nil?")
  (closed? [it] true))
(defmacro either-or-mac
  "A macro in case 'alt' shouldn't be evaluated unless its needed.
  Returns a map with either the original value under the key, or with the alt value under the key."
  [mappo keybit alt]
  `(if (get ~mappo ~keybit)
     ~mappo
     (into ~mappo {~keybit ~alt})))
(defn either-or
  "Use instead of macro any time it's no big deal if the alt form is evaluated.
  Returns a map with either the original value under the key, or with the alt value under the key."
  [mappo keybit alt]
  ;(println "either-or-ing on " mappo " and " keybit ", which is " (get mappo keybit) " with alt " alt )
  (either-or-mac mappo keybit alt))


(defn- put-state-map! [node propmap]
  (-> node
      (.getProperties)
      (.put :node-data propmap)))

(defn either-or-set! [mappo node keybit alt]
  (let [newmap (either-or mappo keybit alt)
        actual-val (get keybit mappo)]
    (when (nil? (get mappo keybit)) (put-state-map! node newmap))
    newmap))

(defn- get-state-map [node]
  (-> node
      (.getProperties)
      (.getOrDefault :node-data {})))
(defn construct-base-node-map [node]
  (-> node
      (get-state-map)
      (into {:id (try (.getId node)
                      (catch Exception e
                        (.hashCode node)))})
      (either-or :event-chan nil)
      (either-or-set! node :state-spec ::any)))
(defn construct-labeled-map [^Labeled node]
  {:label (.getText node)})
(defn construct-text-input-map [^TextInputControl node]
  )

(defn make-opt-map-for-selector [node]
  (let [sm (get-state-map node)
        selecto (.getSelectedItem (.getSelectionModel node))
        unstasher (or (:unstash-fn sm) ui/default-reversal-fn)]
    {:state   (if unstasher
                (do
                  (println sm)
                  (unstasher selecto))
                selecto)
     :options (if (:options sm)
                (do (println "making rendered")
                    (:options sm))
                (do (println "getting natural")
                    (into [] (.getItems node))))}))

(defn- change-event-into-chan [node prop-name chan]
  (fx/set-change-listener! node prop-name
                           (fx/change-listener [obval oldval newval]
                                               (go (>! chan {:event-category :state-change,
                                                             :parent         node,
                                                             :old-value      oldval
                                                             :new-value      newval})))))
(defn- maybe-set-chan! [node propname chan]
  (when chan (change-event-into-chan node propname chan)))

(defn maybe-set-id! [node newid] (when newid (.setId node newid)))
(defn maybe-setstate-text! [node newstate] (when newstate (.setText node (str newstate))))
(defn maybe-setstate-toggle! [node newstate] (when (not (nil? newstate)) (.setSelected node newstate)))
(defn maybe-setstate-multiselect! [node newstate] (when newstate (let [selmod (.getSelectionModel node)
                                                                       indof (-> node (.getItems) (.indexOf newstate))]
                                                                   (.clearSelection selmod)
                                                                   (.selectRange selmod indof (inc indof)))))

(defn maybe-setstate-singleselect! [node newstate]
  (when newstate (-> node
                     (.getSelectionModel)
                     (.select newstate))))


(defn has-been-changed? [changed-map]
  (not (empty? changed-map)))

(defn request-generic-set! [this new-node-data]
  (let [orig-map (ui/get-node-data this)
        [_ {:keys [id state event-chan] :as changed} _] (data/diff orig-map new-node-data)]
    (put-state-map! this (merge orig-map new-node-data))
    (maybe-set-id! this id)
    changed))
(defn request-labeled-set! [^Labeled this changed-node-data]
  (when (:label changed-node-data)
    (.setText (:label changed-node-data)))
  changed-node-data)
(defn request-disabled-set [this {:keys [user-editable?] :as changed-node-data}]
  (when user-editable?
    (.setDisable user-editable?))
  changed-node-data)
(defn request-text-set! [this new-node-data]
  (let [{:keys [state event-chan] :as changed} (request-generic-set! this new-node-data)]
    (maybe-set-chan! this :text event-chan)
    (maybe-setstate-text! this state)
    (has-been-changed? changed)))
(defn request-toggle-set! [this new-node-data]
  (let [{:keys [state event-chan] :as changed} (request-generic-set! this new-node-data)]
    (maybe-set-chan! this :selected event-chan)
    (maybe-setstate-toggle! this state)
    (has-been-changed? changed)))
(defn- print-and-pass [x] (clojure.pprint/pprint x) x)
(defn request-selector-set! [this new-node-data state-set-fn]
  (let [{:keys [state event-chan options rendered-options type render-fn] :as changed} (request-generic-set! this new-node-data)]
    (println "selector set! " new-node-data " and orig is " (get-state-map this))
    (when options
      (let [old-props (get-state-map this)
            props-with-new-opts (into old-props {:options (:options new-node-data)})
            rendfn (:render-fn old-props)]
        (put-state-map! this props-with-new-opts)))
    ;(ui/apply-render-fn! this)

    (when rendered-options
      (let [listo (.getItems this)]
        (println "rendered! " (:rendered-options new-node-data))
        (.clear listo)
        (.addAll listo (:rendered-options new-node-data))))

    (maybe-set-chan! this :selectionModel event-chan)
    (fx/run-later (state-set-fn state))
    (println "render-fn is " render-fn)
    (has-been-changed? changed)))
(defn request-toggle-set! [this new-node-data]
  (let [{:keys [state event-chan options rendered-options type render-fn] :as changed} (request-generic-set! this new-node-data)]
    (when (not (nil? state))
      (.setSelected this state))))




(extend-protocol Atomic-Node
  TextField
  (-ui-get-node-data [this]
    (-> this
        (construct-base-node-map)
        ;(into (construct-labeled-map this))
        (into {:class          [:text]
               :user-editable? (.isEditable this)
               :state          (.getText this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (->> (request-text-set! this new-node-data)
                     ;(request-labeled-set! this)
                     )))

  TextArea
  (-ui-get-node-data [this]
    (-> this
        (construct-base-node-map)
        ;(into (construct-labeled-map this))
        (into {:class          [:text]
               :user-editable? (.isEditable this)
               :state          (.getText this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (->> (request-text-set! this new-node-data)
                     ;(request-labeled-set! this)
                     )))

  Label
  (-ui-get-node-data [this]
    (-> this
        (construct-base-node-map)
        (into (construct-labeled-map this))
        (into {:class          [:text]
               :user-editable? false
               :state          (.getText this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (->> (request-text-set! this new-node-data)
                     (request-labeled-set! this))))

  CheckBox
  (-ui-get-node-data [this]
    (-> this
        (construct-base-node-map)
        (into (construct-labeled-map this))
        (into {:class          [:switch]
               :user-editable? (not (.isDisabled this))
               :state          (.isSelected this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (->> (request-toggle-set! this new-node-data)
                     (request-labeled-set! this))))

  ListView
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into (make-opt-map-for-selector this))
        ;(into (construct-labeled-map this))
        (into {:class          [:option]
               :user-editable? (.isEditable this)})))

  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (request-selector-set! this new-node-data #(maybe-setstate-multiselect! this %))))


  ComboBox
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into (make-opt-map-for-selector this))
        (into (construct-labeled-map this))
        (into {:class          [:option]
               :user-editable? (.isEditable this)})))

  (-ui-request-set-node-data [this new-node-data]
    (let [changes? #(maybe-setstate-singleselect! this %)
          rfn (:render-fn new-node-data)]
      (println "new render fn is " rfn)
      (fx/run-now (request-selector-set! this new-node-data changes?))))




  CheckBox
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into (construct-labeled-map this))
        (into {:class          [:switch]
               :user-editable? (not (.isDisable this))
               :state          (.isSelected this)})))

  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (do (request-toggle-set! this new-node-data)
                    (request-labeled-set! this new-node-data))))

  RadioButton
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into (construct-labeled-map this))
        (into {:class          [:switch]
               :user-editable? (not (.isDisabled this))
               :state          (.isSelected this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (do (request-toggle-set! this new-node-data)
                    (request-labeled-set! this new-node-data))))

  ToggleButton
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into (construct-labeled-map this))
        (into {:class          [:switch]
               :user-editable? (not (.isDisabled this))
               :state          (.isSelected this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (do (request-toggle-set! this new-node-data)
                    (request-labeled-set! this new-node-data))))

  ToggleGroup
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into {:class             [:option :irregular]
               :irregular-reasons ["ToggleGroup can't be a node in the scenegraph because it doesn't extend the Node base class."
                                   "ToggleGroup can't add an arbitrary number of :options, as it is limited by the number of buttons present within it. As a workaround, the system will set up to the number of buttons as user-selectable options, taking strings from :rendered-options to use as labels."
                                   "ToggleGroup doesn't automatically use an :unstash-fn created by draconic.ui/apply-render-fn! to generate a keyword/symbol/whatever when its state is retrieved, as a regular :option node would. As a work-around, a list of keyword/whatever options can be provided before the state is retrieved, and as long as the custom render function generates exactly the strings used as labels in the toggles used it will provide those "]
               :user-editable?    false
               :state             (-> this
                                      (.getSelectedToggle)
                                      (ui/get-node-data)
                                      :label)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (request-selector-set! this new-node-data #(maybe-setstate-singleselect! this %))))

  Rating
  (-ui-get-node-data [this]
    (let [isPartial? (.isPartialRating this)]
      (-> {}
          (into (construct-base-node-map this))
          (into {:class          [:number]
                 :user-editable? (not (.isDisabled this))
                 :type           (if isPartial? :double :int)
                 :state          (let [rating-o (.getRating this)]
                                   (if isPartial? rating-o (int rating-o)))}))))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (do
                  (let [{:keys [type state] :as differs} (request-generic-set! this new-node-data)]
                    (when state (.setRating this state))
                    (when type (case type
                                 :int (.setPartialRating this false)
                                 :double (.setPartialRating this true)))

                    ))))
  Tab
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into {:class          [:container
                                :switch]
               :user-editable? false
               :type           :boolean
               :children       (.getContent this)
               :state          (.isSelected this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (do
                  (let [{:keys [type state children] :as differs}
                        (request-generic-set! this new-node-data)]
                    (when state
                      (.. this getTabPane getSelectionModel (select this)))
                    (when children
                      (.. this setContent))

                    ))))

  ScrollPane
  (-ui-get-node-data [this]
    (-> {}
        (into (construct-base-node-map this))
        (into {:class          [:container]
               :user-editable? false
               :type           :boolean
               :state          (.getContent this)})))
  (-ui-request-set-node-data [this new-node-data]
    (fx/run-now (do
                  (let [{:keys [state] :as differs}
                    (request-generic-set! this new-node-data)]
                    (when state
                      (.setContent this state))))))
  )



(comment

  (do

    (def test-flow (TextFlow.))

    (def test-lv (let [listo (ListView.)]
                   (.setId listo "listo")
                   (get (fxml/launch-fxml-window [(fn [] [listo {"listo" listo}])])
                        "listo")))

    #_(run! #(fx/run-later (-> test-cb (.getItems) (.add %)))
            [:something :nothing :anything :forever])

    (ui/set-options-list! test-lv [:millions :billions :trillions :all :none :many :some :little :few :lots])

    (def test-window (fxml/launch-fxml-window ["toggroup.fxml"]))

    (def test-rating (get (fxml/launch-fxml-window ["rating.fxml"]) "ratingbit"))

    )
  )




;(ui/set-render-fn! test-lv #(str "item:" %))
