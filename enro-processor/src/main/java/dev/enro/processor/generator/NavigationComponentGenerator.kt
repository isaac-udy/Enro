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
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.GeneratedNavigationComponent
import dev.enro.processor.domain.GeneratedBindingReference
import dev.enro.processor.domain.GeneratedModuleReference
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.extends
import dev.enro.processor.extensions.getElementName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.StandardLocation
import com.squareup.javapoet.AnnotationSpec as JavaAnnotationSpec
import com.squareup.javapoet.ClassName as JavaClassName
import com.squareup.javapoet.CodeBlock as JavaCodeBlock
import com.squareup.javapoet.MethodSpec as JavaMethodSpec
import com.squareup.javapoet.ParameterSpec as JavaParameterSpec
import com.squareup.javapoet.TypeSpec as JavaTypeSpec

object NavigationComponentGenerator {
    @OptIn(KspExperimental::class)
    fun generateKotlin(
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
        val isAndroidApplication = declaration
            .getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "android.app.Application" }

        val isNavigationComponentConfiguration = declaration
            .getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "dev.enro.core.NavigationComponentConfiguration" }

        val isEnro3 = declaration
            .getAllSuperTypes()
            .any { it.declaration.qualifiedName?.asString() == "dev.enro3.controller.NavigationControllerConfiguration" }

        if (isEnro3) return

        when {
            isAndroid && isAndroidApplication -> {
                // It's OK for a NavigationComponent to be an application
            }
            !isObject -> {
                val message = when {
                    isAndroid -> "@NavigationComponent can only be applied to objects or classes that " +
                            "extend android.app.Application"
                    else -> "@NavigationComponent can only be applied to objects"
                }
                environment.logger.error(message, declaration)
                error(message)
            }
            !isNavigationComponentConfiguration -> {
                val message = "@NavigationComponent can only be applied to objects that extend " +
                        "NavigationComponentConfiguration"
                environment.logger.error(message, declaration)
                error(message)
            }
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
                ClassName("kotlin", "Function1")
                    .parameterizedBy(
                        ClassNames.Kotlin.navigationModuleScope,
                        ClassNames.Kotlin.unit
                    )
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .returns(Unit::class.java)
                    .addParameter(
                        ParameterSpec
                            .builder(
                                "navigationModuleScope",
                                ClassNames.Kotlin.navigationModuleScope,
                            )
                            .build()
                    )
                    .apply {
                        bindings.forEach {
                            addStatement(
                                "%T().invoke(navigationModuleScope)",
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

        FileSpec
            .builder(
                declaration.packageName.asString(),
                requireNotNull(generatedComponent.name)
            )
            .addType(generatedComponent)
            .build()
            .writeTo(
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

        val functionName = "installNavigationController"
        val extensionName = "${declaration.simpleName.asString()}.$functionName"
        val (platformParameterName, addPlatformParameter) = createPlatformApplicationReferenceParameter(
            resolver,
        )
        val callNavigationComponentConfigModule = when {
            isNavigationComponentConfiguration -> "module(${declaration.simpleName.asString()}.module)"
            else -> ""
        }
        val extensionFunction = FunSpec.builder(functionName)
            .addModifiers(
                when {
                    // on Desktop the application should be installed through the Composable
                    // extension function, which is added later, so we make this function private
                    isDesktop -> KModifier.PRIVATE
                    else -> KModifier.PUBLIC
                }
            )
            .returns(ClassNames.Kotlin.navigationController)
            .addPlatformParameter()
            .addParameter(
                ParameterSpec.builder(
                    "root",
                    ClassNames.Kotlin.navigationInstructionOpenPresent.copy(nullable = true)
                )
                    .defaultValue("null")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("strictMode", Boolean::class)
                    .defaultValue("true")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("useLegacyContainerPresentBehavior", Boolean::class)
                    .defaultValue("false")
                    .build()
            )
            .addParameter(
                ParameterSpec.builder("backConfiguration", ClassNames.Kotlin.enroBackConfiguration)
                    .defaultValue("%T.Default", ClassNames.Kotlin.enroBackConfiguration)
                    .build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "block",
                    LambdaTypeName.get(
                        receiver = ClassNames.Kotlin.navigationModuleScope,
                        returnType = ClassNames.Kotlin.unit
                    )
                )
                    .defaultValue("{}")
                    .build()
            )
            .addCode(
                CodeBlock.of(
                    """
                        val controller = internalCreateNavigationController(
                            strictMode = strictMode,
                            useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                            backConfiguration = backConfiguration,
                            block = {
                                ${generatedComponent.name}().invoke(this)
                                $callNavigationComponentConfigModule
                                block()
                            }
                        )
                        controller.install($platformParameterName)
                        if (root != null) {
                            controller.windowManager.open(root.setOpenInWindow())
                        }
                        return controller
                    """.trimIndent()
                )
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
                                root = root,
                                strictMode = strictMode,
                                useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                                backConfiguration = backConfiguration,
                                block = block,
                            )
                        }
                    """.trimIndent()
                    )
                )
                .build()
        }

        FileSpec
            .builder(
                packageName = declaration.packageName.asString(),
                fileName = extensionName,
            )
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class)
                    .addMember("\"INVISIBLE_REFERENCE\", \"INVISIBLE_MEMBER\"")
                    .build()
            )
            .addImport(
                packageName = "dev.enro.core.controller",
                names = arrayOf("internalCreateNavigationController"),
            )
            .addImport(
                packageName = "dev.enro.core.window",
                names = arrayOf("setOpenInWindow")
            )
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
            .writeTo(
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
                fileName = requireNotNull(extensionName),
            )
    }

    fun generateJava(
        processingEnv: ProcessingEnvironment,
        component: Element,
        generatedModuleName: String?,
        generatedModuleBindings: List<Element>
    ) {
        val isAndroidApplication = component is TypeElement &&
                component.extends(processingEnv, ClassNames.Java.androidApplication)

        val isNavigationComponentConfiguration = component is TypeElement &&
            component.extends(processingEnv, JavaClassName.get("dev.enro.core", "NavigationComponentConfiguration"))

        // This is only a best guess at whether an object is really a Kotlin object, as we
        // don't have full access to Kotlin constructs in KAPT, but this is good enough
        // for the purposes of preventing a user from getting themselves into trouble with KAPT
        val isObject = component is TypeElement
                && component.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { it.simpleName.contentEquals("INSTANCE") }
            .filter { it.modifiers == setOf(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL) }
            .isNotEmpty()

        when {
            isAndroidApplication -> {
                // It's OK for a NavigationComponent to be an application
            }
            !isObject -> {
                val message = "@NavigationComponent can only be applied to objects or classes that " +
                    "extend android.app.Application"
                processingEnv.messager.printError(message, component)
                error(message)
            }
            !isNavigationComponentConfiguration -> {
                val message = "@NavigationComponent can only be applied to objects that extend " +
                        "NavigationComponentConfiguration"
                processingEnv.messager.printError(message, component)
                error(message)
            }
        }

        val modules = GeneratedModuleReference.load(processingEnv)
        val bindings = modules.flatMap { it.bindings }
            .plus(
                generatedModuleBindings.map {
                    val annotation = it.getAnnotation(GeneratedNavigationBinding::class.java)
                    GeneratedBindingReference.Java(
                        binding = it.getElementName(processingEnv),
                        destination = annotation.destination,
                        navigationKey = annotation.navigationKey
                    )
                }
            )

        val moduleNames = modules
            .map { "${it.qualifiedName}.class" }
            .let {
                if (generatedModuleName != null) {
                    it + "$generatedModuleName.class"
                } else it
            }
            .joinToString(separator = ",\n")

        val bindingNames = bindings.joinToString(separator = ",\n") { "${it.binding}.class" }

        val packageName = processingEnv.elementUtils.getPackageOf(component).toString()
        val generatedName = "${component.simpleName}Navigation"
        val classBuilder = JavaTypeSpec.classBuilder(generatedName)
            .addOriginatingElement(component)
            .addOriginatingElement(
                processingEnv.elementUtils
                    .getPackageElement(EnroLocation.GENERATED_PACKAGE)
            )
            .apply {
                modules.forEach {
                    addOriginatingElement(it.element)
                }
            }
            .addAnnotation(
                JavaAnnotationSpec.builder(GeneratedNavigationComponent::class.java)
                    .addMember("bindings", "{\n$bindingNames\n}")
                    .addMember("modules", "{\n$moduleNames\n}")
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassNames.Java.kotlinFunctionOne,
                    ClassNames.Java.navigationModuleScope,
                    JavaClassName.get(Unit::class.java)
                )
            )
            .addMethod(
                JavaMethodSpec.methodBuilder("invoke")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(Unit::class.java)
                    .addParameter(
                        JavaParameterSpec
                            .builder(ClassNames.Java.navigationModuleScope, "navigationModuleScope")
                            .build()
                    )
                    .apply {
                        bindings.forEach {
                            addStatement(
                                JavaCodeBlock.of(
                                    "new $1T().invoke(navigationModuleScope)",
                                    JavaClassName.get(
                                        EnroLocation.GENERATED_PACKAGE,
                                        it.binding.split(".").last()
                                    )
                                )
                            )
                        }
                        addStatement(JavaCodeBlock.of("return kotlin.Unit.INSTANCE"))
                    }
                    .build()
            )
            .build()

        JavaFile
            .builder(
                packageName,
                classBuilder
            )
            .build()
            .writeTo(processingEnv.filer)

        val extensionFunctionName = "installNavigationController"
        processingEnv.filer
            .createResource(
                StandardLocation.SOURCE_OUTPUT,
                packageName,
                "${component.simpleName}.$extensionFunctionName.kt",
                component,
            )
            .openWriter()
            .append(
                """
                @file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
                package $packageName
                
                import android.app.Application
                import dev.enro.core.AnyOpenInstruction
                import dev.enro.core.controller.EnroBackConfiguration
                import dev.enro.core.controller.NavigationController
                import dev.enro.core.controller.NavigationModuleScope
                import dev.enro.core.controller.internalCreateNavigationController
                import dev.enro.core.window.isOpenInWindow
                import kotlin.Boolean
                import kotlin.Suppress
                import kotlin.Unit
                
                public fun ${component.simpleName}.$extensionFunctionName(
                  application: Application,
                  root: AnyOpenInstruction? = null,
                  strictMode: Boolean = true,
                  useLegacyContainerPresentBehavior: Boolean = false,
                  backConfiguration: EnroBackConfiguration = EnroBackConfiguration.Default,
                  block: NavigationModuleScope.() -> Unit = {},
                ): NavigationController {
                  val controller = internalCreateNavigationController(
                      strictMode = strictMode,
                      useLegacyContainerPresentBehavior = useLegacyContainerPresentBehavior,
                      backConfiguration = backConfiguration,
                      block = {
                          ${generatedName}().invoke(this)
                          block()
                      }
                  )
                  controller.install(application)
                  if (root != null) {
                      controller.windowManager.open(root.setOpenInWindow())
                  }
                  return controller
                }
                """.trimIndent()
            )
            .close()
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

