package dev.shanty.kotwire.stimulus

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class StimulusController(
    val targets: Array<String> = emptyArray(),
    val values: KClass<*> = Unit::class,
    val events: Array<StimulusEvent> = emptyArray(),
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BridgeController(
    val component: String,
    val targets: Array<String> = emptyArray(),
    val values: KClass<*> = Unit::class,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class StimulusEvent(
    val name: String,
    val payload: KClass<*> = Unit::class,
)
