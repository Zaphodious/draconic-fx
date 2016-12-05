@file:JvmName("draconicKt")

package com.blakwurm.draconic

import clojure.lang.Fn
import clojure.lang.IFn
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import java.util.*





/**
 * The actual code in this file is used for java interop testing from the repl, and should not be considered
 * part of the public API. Also, a Java (or Kotlin) source file loaded in
 * an IDE is useful for working with JavaFX, as working with the library seems to be
 * primarily designed to be done from the auto-complete prompt.
 */

@Deprecated("Strictly used for interop testing")
fun foo(baz: String): String {
    return "baz is $baz"
}

@Deprecated("Strictly used for interop testing")
data class MutExample(var something: String, var another: Int)

@Deprecated("Strictly used for interop testing")
data class LorumIpsumFields(var Lorem: String = "nec",
                            var ipsum: String = "maximus",
                            var dolor: String = "Praesent",
                            var sit: String = "rutrum",
                            var amet: String = "tellus",
                            var consectetur: String = "eget",
                            var adipiscing: String = "est",
                            var elit: String = "aliqua",
                            var Fusce: String = "sed",
                            var eu: String = "hendrerit",
                            var nulla: String = "justo",
                            var non: String = "sagittis",
                            var diam: String = "Aliquam",
                            var volutpat: String = "erat",
                            var aliquet: String = "volutpat",
                            var facilisis: String = "et",
                            var at: String = "ligula",
                            var sapien: String = "semper",
                            var vel: String = "facilisis",
                            var maximus: String = "mauris",
                            var morbi: String = "nec",
                            var tincidunt: String = "sagittis",
                            var turpis: String = "dui"
)

@Deprecated("Strictly used for interop testing")
data class parentThingy(var child1: ChildThingy1 = ChildThingy1(), var anotherChild: ChildAnotherOne = ChildAnotherOne())

@Deprecated("Strictly used for interop testing")
data class ChildThingy1(var yetAnotherChild: ChildAnotherOne = ChildAnotherOne())

@Deprecated("Strictly used for interop testing")
data class ChildAnotherOne(var message: String = "Can You Dig It?", var active: Boolean = true)
