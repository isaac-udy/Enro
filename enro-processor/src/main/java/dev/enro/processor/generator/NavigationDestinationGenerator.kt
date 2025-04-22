package dev.enro.processor.generator

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
                    .addNavigationDestination(destination)
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
        destination: DestinationReference.Kotlin,
    ): FunSpec.Builder {
        return when {
            destination.isActivity -> addCode(
                "navigationModuleScope.activityDestination<%T, %T>()",
                destination.keyType.asStarProjectedType().toTypeName(),
                destination.toClassName(),
            )
            destination.isFragment -> addCode(
                "navigationModuleScope.fragmentDestination<%T, %T>()",
                destination.keyType.asStarProjectedType().toTypeName(),
                destination.toClassName(),
            )
            destination.isSyntheticClass -> addCode(
                "navigationModuleScope.syntheticDestination<%T, %T>()",
                destination.keyType.asStarProjectedType().toTypeName(),
                destination.toClassName(),
            )
            destination.isSyntheticProvider -> addCode(
                "navigationModuleScope.syntheticDestination(%L)",
                requireNotNull(destination.declaration.simpleName).asString(),
            )
            destination.isManagedFlowProvider -> addCode(
                "navigationModuleScope.managedFlowDestination(%L)",
                requireNotNull(destination.declaration.simpleName).asString(),
            )
            destination.isComposable -> addCode(
                "navigationModuleScope.composableDestination<%T> { %L() }",
                destination.keyType.asStarProjectedType().toTypeName(),
                requireNotNull(destination.declaration.simpleName).asString(),
            )
            destination.isDesktopWindow -> addCode(
                "navigationModuleScope.desktopWindowDestination<%T, %T> { %T() }",
                destination.keyType.asStarProjectedType().toTypeName(),
                destination.toClassName(),
                destination.toClassName(),
            )
            else -> error("${destination.declaration.qualifiedName?.asString()}")
        }
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
        addStatement(
            when {
                destination.isActivity -> JavaCodeBlock.of(
                    """
                    navigationModuleScope.binding(
                        createActivityNavigationBinding(
                            $1T.class,
                            $2T.class
                        )
                    )
                """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    destination.element
                )

                destination.isFragment -> JavaCodeBlock.of(
                    """
                    navigationModuleScope.binding(
                        createFragmentNavigationBinding(
                            $1T.class,
                            $2T.class
                        )
                    )
                """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    destination.element
                )

                destination.isSyntheticClass -> JavaCodeBlock.of(
                    """
                    navigationModuleScope.binding(
                        createSyntheticNavigationBinding(
                            $1T.class,
                            () -> new $2T()
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
                    destination.element
                )
                destination.isSyntheticProvider -> JavaCodeBlock.of(
                    """
                        navigationModuleScope.binding(
                            createSyntheticNavigationBinding(
                                $1T.class,
                                $2T.${destination.originalElement.simpleName.removeSuffix("\$annotations")}()
                            )
                        )
                    """.trimIndent(),
                    JavaClassName.get(destination.keyType),
                    JavaClassName.get(destination.element as TypeElement)
                )
                destination.isManagedFlowProvider -> JavaCodeBlock.of(
                    """
                        navigationModuleScope.binding(
                            createManagedFlowNavigationBinding(
                                $1T.class,
                                $2T.${destination.originalElement.simpleName.removeSuffix("\$annotations")}()
                            )
                        )
                    """.trimIndent(),
                    JavaClassName.get(destination.keyType),
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
                                $composableWrapper.class
                            )
                        )
                    """.trimIndent(),
                        JavaClassName.get(destination.keyType)
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