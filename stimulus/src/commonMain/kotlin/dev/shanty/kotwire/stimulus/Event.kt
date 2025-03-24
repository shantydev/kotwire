package dev.shanty.kotwire.stimulus

interface Event<T : Any> {
    val name: String
    val payload: T
}

