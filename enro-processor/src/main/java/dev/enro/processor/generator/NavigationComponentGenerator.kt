package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationComponent
import dev.enro.processor.domain.ComponentReference
import dev.enro.processor.domain.GeneratedBindingReference
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.chainIf

object NavigationComponentGenerator {

    @OptIn(KspExperimental::class)
    fun generate(
        environment: SymbolProcessorEnvironment,
        platform: ResolverPlatform,
        component: ComponentReference,
        bindings: List<GeneratedBindingReference>,
    ) {
        val isIos = platform is ResolverPlatform.Ios
        val isDesktop = platform is ResolverPlatform.JvmDesktop
        val isAndroid = platform is ResolverPlatform.Android


        val bindingNames = bindings.joinToString(separator = ",\n") {
            "${it.qualifiedName}::class"
        }

        val generatedName = "${component.simpleName}Navigation"
        val generatedComponent = TypeSpec.classBuilder(generatedName)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationComponent::class.java)
                    .addMember("bindings = [\n$bindingNames\n]")
                    .build()
            )
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(
                ClassName("dev.enro.controller", "NavigationModuleAction")
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .receiver(ClassName("dev.enro.controller", "NavigationModule.BuilderScope"))
                    .returns(Unit::class.java)
                    .apply {
                        bindings.forEach {
                            addStatement(
                                "%T().apply { invoke() }",
                                ClassName(
                                    EnroLocation.GENERATED_PACKAGE,
                                    it.qualifiedName.split(".").last()
                                )
                            )
                        }
                    }
                    .build()
            )
            .build()

        // Generate extension function for installing navigation controller
        val functionName = "installNavigationController"
        val (platformParameterName, addPlatformParameter) = createPlatformApplicationReferenceParameter(platform)

        val extensionFunction = FunSpec.builder(functionName)
            .addModifiers(KModifier.PUBLIC)
            .returns(ClassNames.Kotlin.navigationController)
            .addPlatformParameter()
            .addParameter(
                ParameterSpec.builder(
                    "block",
                    LambdaTypeName.get(
                        receiver = ClassName("dev.enro.controller", "NavigationModule.BuilderScope"),
                        returnType = ClassNames.Kotlin.unit
                    )
                )
                    .defaultValue("{}")
                    .build()
            )
            .chainIf(isAndroid) {
                addCode(
                    """
                        // If we're installing in an Android context, we know that we
                        // can use Log.e, so we force EnroLog to use Android Logs during
                        // installation so we log anything that happens during installation,
                        // and we then reset the "force" afterwards.
                        // It's important to reset it, as we might be running in a Robolectric
                        // test, and other non-Robolectric tests might need to use the default logging.
                        dev.enro.platform.EnroLog.forceAndroidLogs = true
                    """.trimIndent() + "\n"
                )
            }
            .addCode(
                """
                    val controller = internalCreateEnroController(
                        builder = {
                            ${generatedComponent.name}().apply { 
                                module(module)
                                invoke() 
                            }
                            block()
                        }
                    )
                    controller.install($platformParameterName)
                """.trimIndent() + "\n"
            )
            .chainIf(isAndroid) {
                addCode(
                    """
                        dev.enro.platform.EnroLog.forceAndroidLogs = false
                    """.trimIndent() + "\n"
                )
            }
            .addCode(
                """
                    return controller
                """.trimIndent()
            )
            .receiver(component.className)
            .build()

        val desktopFunction = when {
            !isDesktop -> null
            else -> extensionFunction.toBuilder("rememberNavigationController")
                .apply { modifiers.clear() }
                .apply {
                    val updatedParameters =
                        parameters.filterNot { it.name == platformParameterName }
                    parameters.clear()
                    parameters.addAll(updatedParameters)
                }
                .addModifiers(KModifier.PUBLIC)
                .addAnnotation(ClassNames.Kotlin.composable)
                .clearBody()
                .addCode(
                    CodeBlock.of(
                        """
                        return androidx.compose.runtime.remember {
                            installNavigationController(
                                $platformParameterName = Unit,
                                block = block,
                            )
                        }
                    """.trimIndent()
                    )
                )
                .build()
        }

        val fileSpec = FileSpec
            .builder(
                component.className.packageName,
                requireNotNull(generatedComponent.name)
            )
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("\"INVISIBLE_REFERENCE\", \"INVISIBLE_MEMBER\"")
                    .build()
            )
            .addImport("dev.enro.controller", "NavigationModule")
            .addImport(
                packageName = "dev.enro.controller",
                names = arrayOf("internalCreateEnroController"),
            )
            .addType(generatedComponent)
            .addFunction(extensionFunction)
            .let {
                if (desktopFunction == null) return@let it
                it.addFunction(desktopFunction)
            }
            .build()

        fileSpec.writeTo(
            codeGenerator = environment.codeGenerator,
            dependencies = Dependencies(
                aggregating = true,
                sources = bindings.mapNotNull { it.containingFile }
                    .plus(listOfNotNull(component.containingFile))
                    .toTypedArray()
            )
        )
    }

    @OptIn(KspExperimental::class)
    fun createPlatformApplicationReferenceParameter(
        resolverPlatform: ResolverPlatform
    ): Pair<String, FunSpec.Builder.() -> FunSpec.Builder> {
        when {
            resolverPlatform is ResolverPlatform.Android -> {
                return "application" to {
                    addParameter(
                        ParameterSpec.builder(
                            "application",
                            resolverPlatform.androidApplicationClassName,
                        ).build()
                    )
                }
            }

            resolverPlatform is ResolverPlatform.JvmDesktop -> {
                return "ignored" to {
                    addParameter(
                        ParameterSpec.builder(
                            "ignored",
                            ClassNames.Kotlin.unit,
                        ).build()
                    )
                }
            }

            resolverPlatform is ResolverPlatform.WasmJs -> {
                return "document" to {
                    addParameter(
                        ParameterSpec.builder(
                            "document",
                            resolverPlatform.webDocumentClassName,
                        ).build()
                    )
                }
            }

            resolverPlatform is ResolverPlatform.Ios -> {
                return "application" to {
                    addParameter(
                        ParameterSpec.builder(
                            "application",
                            resolverPlatform.uiApplicationClassName,
                        ).build()
                    )
                }
            }

            else -> {
                error("Unsupported platform!")
            }
        }
    }
}
