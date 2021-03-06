package ru.nordbird.tfsmessenger.ui.recycler.base

interface ViewTyped {
    val viewType: Int
        get() = error("provide viewType $this")

    val uid: String
        get() = error("provide uid for viewType $this")

    fun asString(): String
}