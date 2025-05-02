package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.processor.domain.DestinationReference
import dev.enro.processor.extensions.ClassNames
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.getElementName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import com.squareup.javapoet.AnnotationSpec as JavaAnnotationSpec
import com.squareup.javapoet.ClassName as JavaClassName
import com.squareup.javapoet.CodeBlock as JavaCodeBlock
import com.squareup.javapoet.ParameterSpec as JavaParameterSpec
import com.squareup.javapoet.TypeSpec as JavaTypeSpec

object NavigationDestinationGenerator {

    @OptIn(KspExperimental::class)
    fun generateKotlin(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        declaration: KSDeclaration
    ) {
        val destination = DestinationReference.Kotlin(resolver, declaration)

        val typeSpec = TypeSpec.classBuilder(destination.bindingName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(
                ClassName("kotlin", "Function1")
                    .parameterizedBy(
                        ClassNames.Kotlin.navigationModuleScope,
                        ClassNames.Kotlin.unit,
                    )
            )
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination = %L",
                        CodeBlock.of("\"${requireNotNull(declaration.qualifiedName).asString()}\"")
                    )
                    .addMember(
                        "navigationKey = %L",
                        CodeBlock.of("\"${requireNotNull(destination.keyType.qualifiedName).asString()}\"")
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                    .returns(Unit::class.java)
                    .addParameter(
                        ParameterSpec
                            .builder(
                                "navigationModuleScope",
                                ClassNames.Kotlin.navigationModuleScope
                            )
                            .build()
                    )
                    .addNavigationDestination(
                        environment = environment,
                        destination = destination,
                    )
                    .build()
            )
            .build()

        FileSpec
            .builder(EnroLocation.GENERATED_PACKAGE, requireNotNull(typeSpec.name))
            .addType(typeSpec)
            .addImport(
                declaration.packageName.asString(),
                requireNotNull(declaration.qualifiedName).asString()
                    .removePrefix(declaration.packageName.asString())
            )
            .addImportsForBinding(destination)
            .build()
            .writeTo(
                codeGenerator = environment.codeGenerator,
                dependencies = Dependencies(
                    aggregating = false,
                    sources = arrayOf(requireNotNull(declaration.containingFile)),
                )
            )
    }

    private fun FunSpec.Builder.addNavigationDestination(
        environment: SymbolProcessorEnvironment,
        destination: DestinationReference.Kotlin,
    ): FunSpec.Builder {
        when (destination.isPlatformDestination) {
            true -> addCode("navigationModuleScope.platformOverrides(\n\tnavigationModuleScope = {\n\t\t")
            else -> addCode("navigationModuleScope.")
        }
        val formatting = LinkedHashMap<String, Any>()
        formatting["keyType"] = destination.keyType.asStarProjectedType().toTypeName()
        formatting["keyName"] = destination.keyType.toClassName()
        val destinationName = when {
            destination.isClass -> {
                formatting["destinationType"] = destination.toClassName()
                "%destinationType:T"
            }
            destination.isFunction -> {
                formatting["destinationFun"] = destination.declaration.simpleName.asString()
                "%destinationFun:L"
            }
            destination.isProperty -> {
                formatting["destinationProp"] = destination.declaration.simpleName.asString()
                "%destinationProp:L"
            }
            else -> {
                environment.logger.error(
                    "Could not generate NavigationDestination for ${destination.declaration.qualifiedName?.asString()}. " +
                            "This is likely because the destination is not a class, function or property."
                )
                "INVALID_DESTINATION_IS_NOT_CLASS_OR_FUNCTION"
            }
        }
        val serializer = when {
            destination.keyIsParcelable -> "dev.enro.core.serialization.SerializerForParcelableNavigationKey(%keyName:T::class)"
            destination.keyIsKotlinSerializable -> "%keyType:T.serializer()"
            else -> {
                environment.logger.error(
                    "Could not generate NavigationDestination for " +
                            "NavigationKey of type ${destination.keyType.simpleName}. This NavigationKey does" +
                            " not implement Parcelable and is not annotated with @kotlinx.serialization.Serializable." +
                            " All NavigationKeys must implement Parcelable or be annotated with @kotlinx.serialization.Serializable."
                )
                "NAVIGATION_KEY_IS_NOT_SERIALIZABLE_OR_PARCELABLE"
            }
        }
        when {
            destination.isActivity -> addNamedCode(
                "activityDestination<%keyType:T, $destinationName>(keySerializer = $serializer)",
                formatting,
            )
            destination.isFragment -> addNamedCode(
                "fragmentDestination<%keyType:T, $destinationName>(keySerializer = $serializer)",
                formatting,
            )
            destination.isSyntheticClass -> addNamedCode(
                "syntheticDestination<%keyType:T, $destinationName>(keySerializer = $serializer) { $destinationName() }",
                formatting,
            )
            destination.isSyntheticProvider -> addNamedCode(
                "syntheticDestination(keySerializer = $serializer, provider = $destinationName)",
                formatting,
            )
            destination.isManagedFlowProvider -> addNamedCode(
                "managedFlowDestination(keySerializer = $serializer, provider = $destinationName)",
                formatting,
            )
            destination.isComposable -> addNamedCode(
                "composableDestination<%keyType:T>(keySerializer = $serializer) { $destinationName() }",
                formatting,
            )
            destination.isDesktopWindow -> addNamedCode(
                "desktopWindowDestination<%keyType:T, $destinationName>(keySerializer = $serializer) { $destinationName() }",
                formatting,
            )
            destination.isUIViewControllerClass -> addNamedCode(
                "uiViewControllerDestination<%keyType:T, $destinationName>(keySerializer = $serializer) { $destinationName() }",
                formatting,
            )
            destination.isUIViewControllerFunction -> addNamedCode(
                "uiViewControllerDestination<%keyType:T, platform.UIKit.UIViewController>(keySerializer = $serializer) { $destinationName() }",
                formatting,
            )
            else -> {
                environment.logger.error(
                    "${destination.declaration.qualifiedName?.asString()} is not a valid destination."
                )
            }
        }
        if (destination.isPlatformDestination) {
            addCode("\n\t}\n)")
        }
        return this
    }

    fun generateJava(
        processingEnv: ProcessingEnvironment,
        element: Element
    ) {
        val destination = DestinationReference.Java(
            processingEnv,
            element
        )

        val classBuilder = JavaTypeSpec.classBuilder(destination.bindingName)
            .addOriginatingElement(element)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassNames.Java.kotlinFunctionOne,
                    ClassNames.Java.navigationModuleScope,
                    JavaClassName.get(Unit::class.java)
                )
            )
            .addAnnotation(
                JavaAnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination",
                        JavaCodeBlock.of("\"${destination.element.getElementName(processingEnv)}\"")
                    )
                    .addMember(
                        "navigationKey",
                        JavaCodeBlock.of("\"${destination.keyType.getElementName(processingEnv)}\"")
                    )
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("invoke")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(Unit::class.java)
                    .addParameter(
                        JavaParameterSpec
                            .builder(ClassNames.Java.navigationModuleScope, "navigationModuleScope")
                            .build()
                    )
                    .addNavigationDestination(processingEnv, destination)
                    .build()
            )
            .build()

        JavaFile
            .builder(EnroLocation.GENERATED_PACKAGE, classBuilder)
            .addImportsForBinding()
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun MethodSpec.Builder.addNavigationDestination(
        processingEnv: ProcessingEnvironment,
        destination: DestinationReference.Java,
    ): MethodSpec.Builder {
        val serializer = when {
            destination.keyIsParcelable -> "dev.enro.core.serialization.SerializerForParcelableNavigationKey"
            destination.keyIsKotlinSerializable -> "dev.enro.core.serialization.DefaultSerializer"
            else -> {
                processingEnv.messager.printError(
                    "Could not generate NavigationDestination for " +
                            "NavigationKey of type ${destination.keyType.simpleName}. This NavigationKey does" +
                            " not implement Parcelable and is not annotated with @kotlinx.serialization.Serializable." +
                            " All NavigationKeys must implement Parcelable or be annotated with @kotlinx.serialization.Serializable."
                )
                "NAVIGATION_KEY_IS_NOT_SERIALIZABLE_OR_PARCELABLE"
            }
        }
        addStatement(
            when {
                destination.isActivity -> JavaCodeBlock.of(
                    """
                    navigationModuleScope.binding(
                        createActivityNavigationBinding(
                            $1T.class,
                            ${serializer}.create($2L.class),
                            $3T.class
                        )
                    )
                """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    JavaClassName.get(destination.keyType).simpleName(),
                    destination.element
                )

                destination.isFragment -> JavaCodeBlock.of(
                    """
                    navigationModuleScope.binding(
                        createFragmentNavigationBinding(
                            $1T.class,
                            ${serializer}.create($2L.class),
                            $3T.class
                        )
                    )
                """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    JavaClassName.get(destination.keyType).simpleName(),
                    destination.element
                )

                destination.isSyntheticClass -> JavaCodeBlock.of(
                    """
                    navigationModuleScope.binding(
                        createSyntheticNavigationBinding(
                            $1T.class,
                            ${serializer}.create($2L.class),
                            () -> new $3T()
                        )
                    )
                """.trimIndent(),
                    JavaClassName.get((destination.keyType).apply {
                        if (typeParameters.isNotEmpty()) {
                            processingEnv.messager.printMessage(
                                Diagnostic.Kind.ERROR,
                                "${destination.keyType.qualifiedName} has generic type parameters, and is bound to a SyntheticDestination. " +
                                        "Type parameters are not supported for SyntheticDestinations as this time"
                            )
                        }
                    }),
                    JavaClassName.get(destination.element as TypeElement).simpleName(),
                    destination.element,
                )
                destination.isSyntheticProvider -> JavaCodeBlock.of(
                    """
                        navigationModuleScope.binding(
                            createSyntheticNavigationBinding(
                                $1T.class,
                                ${serializer}.create($2L.class),
                                $3T.${destination.originalElement.simpleName.removeSuffix("\$annotations")}()
                            )
                        )
                    """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    JavaClassName.get(destination.keyType).simpleName(),
                    JavaClassName.get(destination.element as TypeElement)
                )
                destination.isManagedFlowProvider -> JavaCodeBlock.of(
                    """
                        navigationModuleScope.binding(
                            createManagedFlowNavigationBinding(
                                $1T.class,
                                ${serializer}.create($2L.class),
                                $3T.${destination.originalElement.simpleName.removeSuffix("\$annotations")}()
                            )
                        )
                    """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    JavaClassName.get(destination.keyType).simpleName(),
                    JavaClassName.get(destination.element as TypeElement)
                )
                destination.isComposable -> {
                    val composableWrapper = ComposableWrapperGenerator.generate(
                        processingEnv = processingEnv,
                        element = destination.element as ExecutableElement,
                        keyType = destination.keyType,
                    )
                    JavaCodeBlock.of(
                        """
                        navigationModuleScope.binding(
                            createComposableNavigationBinding(
                                $1T.class,
                                ${serializer}.create($2L.class),
                                $composableWrapper.class,
                                () -> new $composableWrapper()
                            )
                        )
                    """.trimIndent(),
                        JavaClassName.get(destination.keyType),
                        JavaClassName.get(destination.keyType).simpleName()
                    )
                }
                else -> {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "${destination.element.simpleName} does not extend Fragment, FragmentActivity, or SyntheticDestination"
                    )
                    JavaCodeBlock.of(
                        """
                        // Error: ${destination.element.simpleName} does not extend Fragment, FragmentActivity, or SyntheticDestination
                    """.trimIndent()
                    )
                }
            }
        )
        addStatement(JavaCodeBlock.of("return kotlin.Unit.INSTANCE"))
        return this
    }
}

fun JavaFile.Builder.addImportsForBinding(): JavaFile.Builder {
    return this
        .addStaticImport(
            ClassNames.Java.activityNavigationBindingKt,
            "createActivityNavigationBinding"
        )
        .addStaticImport(
            ClassNames.Java.fragmentNavigationBindingKt,
            "createFragmentNavigationBinding"
        )
        .addStaticImport(
            ClassNames.Java.syntheticNavigationBindingKt,
            "createSyntheticNavigationBinding"
        )
        .addStaticImport(
            ClassNames.Java.managedFlowNavigationBindingKt,
            "createManagedFlowNavigationBinding"
        )
        .addStaticImport(
            ClassNames.Java.composeNavigationBindingKt,
            "createComposableNavigationBinding"
        )
        .addStaticImport(
            ClassNames.Java.jvmClassMappings,
            "getKotlinClass"
        )
}

fun FileSpec.Builder.addImportsForBinding(destination: DestinationReference.Kotlin): FileSpec.Builder {
    return this
        .let {
            if (destination.isActivity) {
                it.addImport(
                    "dev.enro.core.activity",
                    "activityDestination"
                )
            } else it
        }
        .let {
            if (destination.isFragment) {
                it.addImport(
                    "dev.enro.destination.fragment",
                    "fragmentDestination"
                )
            } else it
        }
        .let {
            if (destination.isDesktopWindow) {
                it.addImport(
                    "dev.enro.destination.desktop",
                    "desktopWindowDestination"
                )
            } else it
        }
        .let {
            if (destination.isUIViewControllerClass || destination.isUIViewControllerFunction) {
                it.addImport(
                    "dev.enro.destination.uiviewcontroller",
                    "uiViewControllerDestination",
                )
            } else it
        }
        .addImport(
            "dev.enro.destination.synthetic",
            "syntheticDestination"
        )
        .addImport(
            "dev.enro.destination.flow",
            "managedFlowDestination"
        )
        .addImport(
            "dev.enro.destination.compose",
            "composableDestination"
        )
}