package dev.shanty.kotwire.stimulus.ksp

import com.google.devtools.ksp.symbol.KSType

sealed class Value(
    val name: String,
    val type: String,
    val optional: Boolean,
) {
    class StringValue(name: String, optional: Boolean) : Value(name, "String", optional)

    class NumberValue(name: String, optional: Boolean) : Value(name, "Number", optional)

    class BooleanValue(name: String, optional: Boolean) : Value(name, "Boolean", optional)

    class ArrayValue(name: String, val parameterType: KSType, optional: Boolean) : Value(name, "Array", optional)

    class ObjectValue(name: String, val ksType: KSType, optional: Boolean) : Value(name, "Object", optional)
}
