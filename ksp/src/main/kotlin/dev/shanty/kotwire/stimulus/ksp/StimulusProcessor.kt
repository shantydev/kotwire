package dev.shanty.kotwire.stimulus.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.shanty.kotwire.stimulus.BridgeController
import dev.shanty.kotwire.stimulus.ControllerScopeMarker
import dev.shanty.kotwire.stimulus.Event
import dev.shanty.kotwire.stimulus.StimulusController
import kotlinx.datetime.LocalDateTime
import java.io.File

class StimulusProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val jvmOutputDir: File,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val controllers = resolver.findControllers().map {
            generateStimulusController(it, resolver)
        }.plus(
            resolver.findBridgeControllers().map {
                generateStimulusController(it, resolver)
            }
        ).toList()

        if(controllers.isEmpty()) {
            return emptyList()
        }

        // Generate JS Init function
        val initFunc = FunSpec.builder("setupStimulus")
            .returns(ClassName("dev.shanty.kotwire.stimulus", "Application"))
            .addCode("""
                val stimulus = Application.start()
                ${controllers.joinToString("\n") { "${it.factoryFunc.name}(stimulus)" }}
                window.asDynamic()["Stimulus"] = stimulus
                return stimulus
            """.trimIndent())
            .build()

        val initFile = FileSpec.builder("", "init")
            .addFunction(initFunc)
            .addImport("dev.shanty.kotwire.stimulus", "Application")
            .addImport("kotlinx.browser", "window")
            .build()

        initFile.writeTo(codeGenerator, Dependencies(false))
        return emptyList()
    }

    private fun Resolver.findControllers() = getSymbolsWithAnnotation(StimulusController::class.qualifiedName!!)
        .filterIsInstance<KSClassDeclaration>()

    private fun Resolver.findBridgeControllers() = getSymbolsWithAnnotation(BridgeController::class.qualifiedName!!)
        .filterIsInstance<KSClassDeclaration>()

    private fun generateStimulusController(controllerClass: KSClassDeclaration, resolver: Resolver): StimulusControllerFactory {
        // Generate JS
        val annotation = controllerClass.getControllerAnnotation() ?:
        controllerClass.getBridgeComponentAnnotation() ?:
        error("No StimulusController annotation found on ${controllerClass.simpleName.asString()}")

        val packageName = controllerClass.containingFile!!.packageName.asString()
        val className = controllerClass.simpleName.asString()
        val controllerNameWithoutSuffix = controllerClass.simpleName.asString().removeSuffix("Controller").lowercase()

        logger.info("Generating Stimulus Controller $className")

        val targets = annotation.targets()
        val valueClass = annotation.values()
        val component = annotation.component()
        val events = annotation.events()

        // Get list of all properties on class
        val values =
            valueClass?.getAllProperties()?.map {
                val name = it.simpleName.asString()
                val type = it.type.resolve()

                when (type.declaration.qualifiedName!!.asString()) {
                    String::class.qualifiedName -> Value.StringValue(name, type.isMarkedNullable)
                    Int::class.qualifiedName -> Value.NumberValue(name, type.isMarkedNullable)
                    Float::class.qualifiedName -> Value.NumberValue(name, type.isMarkedNullable)
                    Double::class.qualifiedName -> Value.NumberValue(name, type.isMarkedNullable)
                    Boolean::class.qualifiedName -> Value.BooleanValue(name, type.isMarkedNullable)
                    LocalDateTime::class.qualifiedName -> Value.StringValue(name, type.isMarkedNullable)

                    List::class.qualifiedName -> {
                        val genericType = type.arguments.first().type!!.resolve()
                        Value.ArrayValue(name, genericType, type.isMarkedNullable)
                    }
                    Array::class.qualifiedName -> {
                        val genericType = type.arguments.first().type!!.resolve()
                        Value.ArrayValue(name, genericType, type.isMarkedNullable)
                    }

                    else -> Value.ObjectValue(name, type, type.isMarkedNullable)
                }
            }?.toList() ?: emptyList()

        val fileBuilder = FileSpec.builder(packageName, className)
        fileBuilder.writeLifecycleHooks(controllerClass, targets, values)
        fileBuilder.writeExtensions(controllerClass, targets, values)
        fileBuilder.writeEventDefinitions(controllerClass, events)
        val factoryFunc = fileBuilder.writeControllerFactory(controllerClass, targets, values, valueClass, component)
        val file = fileBuilder.build()
        file.writeTo(codeGenerator, Dependencies(true, controllerClass.containingFile!!))

        // Generate JVM
        val jvmFile = FileSpec.builder(packageName, className)

        val contextClassName = "${controllerNameWithoutSuffix}ControllerContext".replaceFirstChar { it.uppercase() }
        val contextClassBuilder = TypeSpec.classBuilder(contextClassName)
            .addSuperinterface(ClassName("kotlinx.html", "HtmlBlockTag"), delegate = CodeBlock.of("delegateTag"))
            .primaryConstructor(FunSpec.constructorBuilder().addParameter("delegateTag", ClassName("kotlinx.html", "DIV")).addModifiers(KModifier.INTERNAL).build())
            .addProperty(PropertySpec.builder("delegateTag", ClassName("kotlinx.html", "DIV"), KModifier.PRIVATE).delegate("delegateTag").build())

        values.forEach {
            val property: PropertySpec = when(it) {
                is Value.BooleanValue -> PropertySpec.builder(it.name, Boolean::class.asTypeName())
                    .mutable(true)
                    .setter(FunSpec.setterBuilder().addParameter("it", Boolean::class.asTypeName()).addCode("""
                        attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] = it.toString()
                    """.trimIndent()).build())
                    .getter(FunSpec.getterBuilder().addCode("""
                        return attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"]?.toBoolean() ?: false
                    """.trimIndent()).build())
                    .build()

                is Value.NumberValue -> PropertySpec.builder(it.name, Double::class.asTypeName())
                    .mutable(true)
                    .setter(FunSpec.setterBuilder().addParameter("it", Double::class.asTypeName()).addCode("""
                        attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] = it.toString()
                    """.trimIndent()).build())
                    .getter(FunSpec.getterBuilder().addCode("""
                        return attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"]?.toDouble() ?: 0.0
                    """.trimIndent()).build())
                    .build()

                is Value.StringValue -> PropertySpec.builder(it.name, String::class.asTypeName())
                    .mutable(true)
                    .setter(FunSpec.setterBuilder().addParameter("it", String::class.asTypeName()).addCode("""
                        attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] = it
                    """.trimIndent()).build())
                    .getter(FunSpec.getterBuilder().addCode("""
                        return attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] ?: ""
                    """.trimIndent()).build())
                    .build()

                is Value.ObjectValue -> {
                    val type = it.ksType.toTypeName()
                    jvmFile.addImport("kotlinx.serialization", "encodeToString")
                    jvmFile.addImport("kotlinx.serialization", "decodeFromString")
                    jvmFile.addImport("kotlinx.serialization.json", "Json")

                    PropertySpec.builder(it.name, type)
                        .mutable(true)
                        .setter(
                            FunSpec.setterBuilder().addParameter("it", type).addCode(
                                """
                        attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] = Json.encodeToString(it)
                    """.trimIndent()
                            ).build()
                        )
                        .getter(
                            FunSpec.getterBuilder().addCode(
                                """
                        return attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"]?.let { Json.decodeFromString(it) }!!
                    """.trimIndent()
                            ).build()
                        )
                        .build()
                }

                is Value.ArrayValue -> {
                    val parameterType = it.parameterType.toTypeName()
                    val type = ClassName("kotlin", "Array").parameterizedBy(parameterType)
                    jvmFile.addImport("kotlinx.serialization", "encodeToString")
                    jvmFile.addImport("kotlinx.serialization", "decodeFromString")
                    jvmFile.addImport("kotlinx.serialization.json", "Json")

                    PropertySpec.builder(it.name, type)
                        .mutable(true)
                        .setter(
                            FunSpec.setterBuilder()
                                .addParameter("it", type)
                                .addStatement("""attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] = Json.encodeToString<%T>(it)""", type)
                                .build()
                        )
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("""val raw = attributes["data-${controllerNameWithoutSuffix}-${it.name}-value"] ?: error("Value not set")""")
                                .addStatement("""return Json.decodeFromString<%T>(raw)""", type)
                                .build()
                        ).build()
                }
            }

            contextClassBuilder.addProperty(property)
        }
        contextClassBuilder.addAnnotation(ControllerScopeMarker::class)

        targets.forEach {
            val targetName = "${it}Target"
            contextClassBuilder.addFunction(
                FunSpec.builder("${targetName}")
                    .addParameter("element", ClassName("kotlinx.html", "HTMLTag"))
                    .addModifiers(KModifier.PUBLIC)
                    .addCode("""
                        element.attributes["data-${controllerNameWithoutSuffix}-target"] = "$it"
                    """.trimIndent())
                    .build(),
            )
        }

        val contextClass = contextClassBuilder.build()
        jvmFile.addType(contextClass)

        jvmFile.addFunction(
            FunSpec.builder(className.replaceFirstChar { it.lowercase() })
                .receiver(ClassName("kotlinx.html", "FlowContent"))
                .addParameter(
                    ParameterSpec.builder(
                        "block",
                        LambdaTypeName.get(
                            receiver = ClassName(packageName, contextClassName),
                            returnType = UNIT,
                        )
                    ).build()
                ).addCode(CodeBlock.of(
                    """
                        div {
                            dataController = "$controllerNameWithoutSuffix"
                            val context = ${contextClassName}(this)
                            block(context)
                        }
                    """.trimIndent()
                )).build()
        )

        jvmFile.addImport("kotlinx.html", "div")
        jvmFile.addImport("dev.shanty.kotwire", "dataController")
        jvmFile.build().writeTo(jvmOutputDir)

        return StimulusControllerFactory(
            factoryFunc,
            file,
        )
    }

    private fun FileSpec.Builder.writeLifecycleHooks(
        controllerClass: KSClassDeclaration,
        targets: List<String>,
        values: List<Value>,
    ) {
        val interfaceBuilder = TypeSpec.interfaceBuilder("${controllerClass.getControllerName()}TargetLifecycleHooks")

        interfaceBuilder.addAnnotation(
            ClassName("kotlin.js", "JsExport"),
        )

        targets.forEach {
            interfaceBuilder.addFunction(
                FunSpec.builder("${it}TargetConnected")
                    .addParameter("element", ClassName("org.w3c.dom", "HTMLElement"))
                    .build(),
            )

            interfaceBuilder.addFunction(
                FunSpec.builder("${it}TargetDisconnected")
                    .addParameter("element", ClassName("org.w3c.dom", "HTMLElement"))
                    .build(),
            )
        }

        values.forEach { value ->
            val variableType =
                when (value) {
                    is Value.ArrayValue -> ClassName("kotlin", "Array").parameterizedBy(value.parameterType.toTypeName())
                    is Value.BooleanValue -> Boolean::class.asTypeName()
                    is Value.NumberValue -> Double::class.asTypeName()
                    is Value.ObjectValue -> value.ksType.toTypeName()
                    is Value.StringValue -> String::class.asTypeName()
                }

            interfaceBuilder.addFunction(
                FunSpec.builder("${value.name}ValueChanged")
                    .addParameter("value", variableType)
                    .addParameter("previousValue", variableType)
                    .build(),
            )
        }

        addType(interfaceBuilder.build())
    }

    private fun FileSpec.Builder.writeExtensions(
        controllerClass: KSClassDeclaration,
        targets: List<String>,
        values: List<Value>,
    ) {
        // Write a delegate for each target as an extension function on the controller class that takes a generic type that extends HTMLElement
        addImport("org.w3c.dom", "HTMLElement")

        targets.forEach {
            val targetName = "${it}Target"
            val typeVariable = TypeVariableName("T", ClassName("org.w3c.dom", "HTMLElement"))
            addFunction(
                FunSpec.builder(targetName)
                    .addTypeVariable(typeVariable)
                    .receiver(controllerClass.asStarProjectedType().toTypeName())
                    .returns(ClassName("", "StimulusProperty").parameterizedBy(typeVariable))
                    .addCode("return StimulusProperty(\"${targetName}\")")
                    .build(),
            )
        }

        values.forEach {
            val valueName = "${it.name}Value"
            val variableType =
                when (it) {
                    is Value.ArrayValue -> ClassName("kotlin", "Array").parameterizedBy(it.parameterType.toTypeName())
                    is Value.BooleanValue -> Boolean::class.asTypeName()
                    is Value.NumberValue -> Double::class.asTypeName()
                    is Value.ObjectValue -> it.ksType.toTypeName()
                    is Value.StringValue -> String::class.asTypeName()
                }

            addFunction(
                FunSpec.builder(valueName)
                    .receiver(controllerClass.asStarProjectedType().toTypeName())
                    .returns(ClassName("", "StimulusProperty").parameterizedBy(variableType))
                    .addCode("return StimulusProperty(\"${valueName}\")")
                    .build(),
            )

            addFunction(
                FunSpec.builder("has${valueName.capitalize()}")
                    .receiver(controllerClass.asStarProjectedType().toTypeName())
                    .returns(Boolean::class.asTypeName())
                    .addCode("""
                        println(this.asDynamic()["has${valueName.capitalize()}"])
                        return this.asDynamic()["has${valueName.capitalize()}"]
                    """.trimIndent())
                    .build(),
            )
        }
    }

    private fun FileSpec.Builder.writeEventDefinitions(
        controllerClass: KSClassDeclaration,
        events: List<EventDefinition>,
    ) {
        addImport("dev.shanty.kotwire.stimulus", "Event")

        events.forEach {

            val className = it.name.capitalize() + "Event"
            val payloadType = it.payload?.asStarProjectedType()?.toTypeName() ?: UNIT

            addType(
                TypeSpec.classBuilder(className)
                    .addSuperinterface(ClassName("dev.shanty.kotwire.stimulus", "Event").parameterizedBy(payloadType))
                    .addProperty(
                        PropertySpec.builder("name", String::class.asTypeName())
                            .initializer("%S", it.name)
                            .addModifiers(KModifier.OVERRIDE)
                            .build(),
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("payload", payloadType)
                            .build(),
                    ).addProperty(
                        PropertySpec.builder("payload", payloadType)
                            .initializer("payload")
                            .addModifiers(KModifier.OVERRIDE)
                            .build(),
                    )
                    .build(),
            )
        }
    }

    private fun FileSpec.Builder.writeControllerFactory(
        controllerClass: KSClassDeclaration,
        targets: List<String>,
        values: List<Value>,
        valueClass: KSClassDeclaration?,
        component: String? = null,
    ): FunSpec {
        addImport("kotlin.reflect", "KClass")
        addImport("", "buildValues")
        addImport("", "addStaticMembersTo")

        val factoryFuncBuilder = FunSpec.builder("register${controllerClass.simpleName.asString()}")

        factoryFuncBuilder.addParameter(
            "stimulus",
            ClassName("dev.shanty.kotwire.stimulus", "Application"),
        )

        factoryFuncBuilder.addCode(
            $$"""
             val clazz = $${controllerClass.qualifiedName!!.asString()}::class
             val properties = object  {
                // Js name needed otherwise Kotlin will mangle the name
                @JsName("targets")
                val targets = $${targets.toArrayStatement()}

                @JsName("values")
                val values = buildValues(
                    setOf(
                        $${values.joinToString(",\n") { "ValueDefinition(\"${it.name}\", \"${it.type}\", ${it.optional})" }}
                    ),
                    $${valueClass?.qualifiedName?.asString()}
                )
                
                $${if(component != null) {
                    """
                        @JsName("component")
                        val component = "$component"
                    """.trimIndent()
                } else ""}
            }

            val factoryFunc = clazz.js.asDynamic()

            addStaticMembersTo(factoryFunc, properties)
            val controllerName = "$${controllerClass.simpleName.asString().removeSuffix("Controller").lowercase()}"
            println("Registering controller $${controllerClass.simpleName.asString().removeSuffix("Controller").lowercase()}")

            stimulus.register(controllerName, factoryFunc)
            """.trimIndent(),
        )

        val factoryFunc = factoryFuncBuilder.build()
        this.addFunction(factoryFunc)

        return factoryFunc
    }

    private fun List<String>.toArrayStatement() =
        when (size) {
            0 -> {
                "js(\"[]\")"
            }
            1 -> {
                """js("['${first()}']")"""
            }
            else -> {
                """js("['${joinToString("', '")}']")"""
            }
        }

    private fun KSClassDeclaration.getControllerName() = simpleName.asString()

    private fun KSClassDeclaration.getControllerAnnotation() = annotations
        .firstOrNull { it.shortName.asString() == StimulusController::class.simpleName }
    private fun KSClassDeclaration.getBridgeComponentAnnotation() = annotations
        .firstOrNull { it.shortName.asString() == BridgeController::class.simpleName }

    private fun KSAnnotation.targets() = arguments.first {
        it.name!!.asString() == "targets"
    }.value as List<String>? ?: emptyList()

    private fun KSAnnotation.values() = (arguments.first {
        it.name!!.asString() == "values"
    }.value as KSType?)?.declaration as KSClassDeclaration?

    private fun KSAnnotation.events() = (arguments.firstOrNull {
        it.name!!.asString() == "events"
    }?.value as List<KSAnnotation>?)?.map {
        EventDefinition(
            it.arguments.first { it.name!!.asString() == "name" }.value as String,
            (it.arguments.firstOrNull { it.name!!.asString() == "payload" }?.value as KSType?)?.declaration as KSClassDeclaration?,
        )
    } ?: emptyList()

    private data class EventDefinition(
        val name: String,
        val payload: KSClassDeclaration?,
    )

    private fun KSAnnotation.component() = (arguments.firstOrNull {
        it.name!!.asString() == "component"
    }?.value as? String)
}

private data class StimulusControllerFactory(
    val factoryFunc: FunSpec,
    val containingFile: FileSpec,
)