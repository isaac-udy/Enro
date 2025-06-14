package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationComponent
import dev.enro.processor.domain.GeneratedModuleReference
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.chainIf

object NavigationComponentGenerator {
    @OptIn(KspExperimental::class)
    fun generate(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        declaration: KSDeclaration,
        resolverBindings: List<KSDeclaration>,
        resolverModules: List<KSDeclaration>,
    ) {
        val isIos = resolver.getKotlinClassByName("platform.UIKit.UIApplication") != null
        val isDesktop = resolver.getKotlinClassByName("androidx.compose.ui.window.ApplicationScope") != null
        val isAndroid = resolver.getKotlinClassByName("android.app.Application") != null

        if (declaration !is KSClassDeclaration) {
            val message = "@NavigationComponent can only be applied to objects"
            environment.logger.error(message, declaration)
            error(message)
        }

        val isObject = declaration.classKind == ClassKind.OBJECT
        val isNavigationComponentConfiguration = declaration
            .getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "dev.enro.controller.NavigationComponentConfiguration" }

        if (!isObject) {
            val message = "@NavigationComponent can only be applied to objects"
            environment.logger.error(message, declaration)
            error(message)
        }

        if (!isNavigationComponentConfiguration) {
            val message = "@NavigationComponent can only be applied to objects that extend " +
                    "NavigationComponentConfiguration"
            environment.logger.error(message, declaration)
            error(message)
        }

        val modules = GeneratedModuleReference.load(resolver)
        val bindings = modules.flatMap { it.bindings }

        val moduleNames = modules.joinToString(separator = ",\n") {
            "${it.qualifiedName}::class"
        }
        val bindingNames = bindings.joinToString(separator = ",\n") {
            "${it.binding}::class"
        }

        val generatedName = "${declaration.simpleName.asString()}Navigation"
        val generatedComponent = TypeSpec.classBuilder(generatedName)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationComponent::class.java)
                    .addMember("bindings = [\n$bindingNames\n]")
                    .addMember("modules = [\n$moduleNames\n]")
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
                                    it.binding.split(".").last()
                                )
                            )
                        }
                    }
                    .build()
            )
            .build()

        // Generate extension function for installing navigation controller
        val functionName = "installNavigationController"
        val (platformParameterName, addPlatformParameter) = createPlatformApplicationReferenceParameter(
            resolver,
        )
        val callNavigationComponentConfigModule = when {
            isNavigationComponentConfiguration -> "module(${declaration.simpleName.asString()}.module)"
            else -> ""
        }

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
            .receiver(
                ClassName(
                    declaration.packageName.asString(),
                    declaration.simpleName.asString()
                )
            )
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
                declaration.packageName.asString(),
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
            .let {
                if (!isIos) return@let it
                it.addFunction(
                    extensionFunction
                        .toBuilder(functionName)
                        .apply {
                            val newParameter = ParameterSpec
                                .builder(
                                    "enro",
                                    ClassNames.Kotlin.enroIosExtensions,
                                )
                                .defaultValue("%T", ClassNames.Kotlin.enroIosExtensions)
                                .build()
                            parameters.add(5, newParameter)
                        }
                        .build()
                )
            }
            .build()

        fileSpec.writeTo(
            codeGenerator = environment.codeGenerator,
            dependencies = Dependencies(
                aggregating = true,
                sources = (resolverModules + resolverBindings).mapNotNull { it.containingFile }
                    .plus(listOfNotNull(declaration.containingFile))
                    .toTypedArray()
            )
        )
        environment.codeGenerator
            .associateWithClasses(
                classes = modules.map { it.declaration },
                packageName = declaration.packageName.asString(),
                fileName = requireNotNull(generatedComponent.name),
            )
    }

    @OptIn(KspExperimental::class)
    fun createPlatformApplicationReferenceParameter(
        resolver: Resolver,
    ): Pair<String, FunSpec.Builder.() -> FunSpec.Builder> {
        val androidApplication = resolver.getKotlinClassByName("android.app.Application")
        val desktopApplication =
            resolver.getKotlinClassByName("androidx.compose.ui.window.ApplicationScope")
        val webDocument = resolver.getKotlinClassByName("org.w3c.dom.Document")
        val iosApplication = resolver.getKotlinClassByName("platform.UIKit.UIApplication")

        when {
            androidApplication != null -> {
                return "application" to {
                    addParameter(
                        ParameterSpec.builder(
                            "application",
                            androidApplication.toClassName(),
                        ).build()
                    )
                }
            }

            desktopApplication != null -> {
                return "ignored" to {
                    addParameter(
                        ParameterSpec.builder(
                            "ignored",
                            ClassNames.Kotlin.unit,
                        ).build()
                    )
                }
            }

            webDocument != null -> {
                return "document" to {
                    addParameter(
                        ParameterSpec.builder(
                            "document",
                            webDocument.toClassName(),
                        ).build()
                    )
                }
            }

            iosApplication != null -> {
                return "application" to {
                    addParameter(
                        ParameterSpec.builder(
                            "application",
                            iosApplication.toClassName(),
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
