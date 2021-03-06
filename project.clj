(defproject draconic-fx "0.5.0-SNAPSHOT"
  :description "A library to help load and program JavaFX apps using FXML as the primary markup language."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-kotlin "0.0.2"]
            [jonase/eastwood "0.2.3"]
            [nightlight/lein-nightlight "1.6.1"]]
  :kotlin-source-path "src/draconic"
  :kotlin-compiler-version "1.0.4"
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.jetbrains.kotlin/kotlin-runtime "1.0.4"]
                 [org.controlsfx/controlsfx "8.40.11"]
                 [com.rpl/specter "0.13.1"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/java.data "0.1.1"]
                 [camel-snake-kebab "0.4.0"]
                 [draconic.ui "0.3.2-SNAPSHOT"]]
  :profiles {:repl {:dependencies [[org.jetbrains.kotlin/kotlin-compiler "1.0.4"]]}}
  :prep-tasks ["kotlin"])
