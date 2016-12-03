# draconic

[![Clojars Project](https://img.shields.io/clojars/v/draconic-fx.svg)](https://clojars.org/draconic-fx)

A Clojure library to help load and program JavaFX apps using FXML. It tries to recreate a bit of the proper JavaFX API's magic-loady controller functionality without having to do any wonky class creation.

Warning- this library is very alpha. What exists is mostly solid due to there not being much, but the API will be extended pretty heavily in time as more functionality is needed. (Oh, and draconic.bean-conversions is basically unusable, so you probably shouldn't use anything in there.)

## Usage

See draconic.fx.ml.clj for an example of how to launch an FXML window. To make your own apps, you'll need the scene builder from Gluon  (http://gluonhq.com/labs/scene-builder/). To work with JavaFX objects directly, you'll need to get familiar with both Java-from-Clojure interop and the JavaFX API.

A word of caution for those of us used to working with immutable data structures- quite a bit of the API is based around adding things to mutable lists, and it can throw you for a loop when you see a method like "getChildren()" when you're trying to figure out how to add nodes to the scene graph. Just thought that I'd warn ya.

## License

Copyright © 2016 Blakwurm Studios

Distributed under the Eclipse Public License 1.0.
