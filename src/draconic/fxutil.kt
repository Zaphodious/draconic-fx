@file:JvmName("draconicKt")

package com.blakwurm.draconic

import clojure.java.api.Clojure
import clojure.lang.*
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.util.Callback
import java.util.*

fun start_app() {
    Application.launch(DraconicApplication::class.java)
}
class DraconicApplication() : Application() {
    override fun start(primaryStage: Stage?) {
        /*println("Thing launched! evalcode is ${this.parameters.raw}")
        var thingy = Clojure.`var`("read-string").invoke(this.parameters.raw.first())
        Clojure.`var`("eval").invoke(thingy)
        val thingmabop = Clojure.`var`(this.parameters.raw.get(0))
        thingmabop.invoke()*/
    }

}

fun getResourceHandle(locstring: String) = locstring.javaClass.getResource(locstring)

fun foo(field: TextField, listener: ChangeListener<String>) {
    field.textProperty().addListener(listener)
}

fun <T> Property<T>.addChangeListener(listener: ChangeListener<T>) {
    this.addListener(listener)
}

fun <I> makeTreeItemCellFactory(fn: IFn) : Callback<TreeView<I>, TreeCell<I>> {
    return Callback {
        object : TreeCell<I>() {
            override fun updateItem(p0: I, p1: Boolean) {
                super.updateItem(p0, p1)
                System.out.print("styles for this are" + this.styleClass)
                this.text = fn(p0) as String?

            }

        } }
}



fun <I> addTreeSelectionListener(treeView: TreeView<I>, iFn: IFn) {
    treeView.selectionModel.selectedItemProperty().addListener { observableValue, oldItem, newItem ->
        iFn(try {
            oldItem.value
        } catch (e: Exception) {
            newItem.value
        }, newItem.value)
    }
}

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
