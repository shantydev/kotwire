import dev.shanty.kotwire.stimulus.Controller
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StimulusProperty<T>(
    private val name: String,
) : ReadWriteProperty<Controller, T> {
    override fun getValue(
        thisRef: Controller,
        property: KProperty<*>,
    ): T {
        val elementDynamic = thisRef.asDynamic()[name]

        if (elementDynamic == undefined) {
            error("Could not find element with name $name")
        }

        return elementDynamic as T
    }

    override fun setValue(
        thisRef: Controller,
        property: KProperty<*>,
        value: T,
    ) {
        thisRef.asDynamic()[name] = value
    }
}

/**
 * @param values Map of property name to the type of the property
 * @param defaults Object with default values for the properties
 */
fun buildValues(
    values: Set<ValueDefinition>,
    defaults: Any?,
): dynamic {
    val defaultsDynamic = defaults.asDynamic()
    val builtObject = js("{}")

    values.asIterable().forEachIndexed { _, (key, type, optional) ->
        if(!optional) {
            val defaultValue = defaultsDynamic[key] ?: defaultForType(type)
            val definition = js("{}")
            definition["default"] = defaultValue
            definition["type"] = stimulusTypeForType(type)
            builtObject[key] = definition
        } else {
            builtObject[key] = stimulusTypeForType(type)
        }
    }

    return builtObject
}

data class ValueDefinition(
    val name: String,
    val type: String,
    val optional: Boolean,
)

fun stimulusTypeForType(type: String): dynamic {
    return when (type) {
        "String" -> js("String")
        "Number" -> js("Number")
        "Boolean" -> js("Boolean")
        "Array" -> js("Array")
        "Object" -> js("Object")
        else -> null
    }
}

fun defaultForType(type: String): Any? {
    return when (type) {
        "String" -> ""
        "Number" -> 0
        "Boolean" -> false
        "Array" -> js("[]")
        "Object" -> js("{}")
        else -> null
    }
}
