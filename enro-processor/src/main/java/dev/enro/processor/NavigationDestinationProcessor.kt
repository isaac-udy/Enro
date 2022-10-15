package dev.enro.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.NavigationDestination
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class NavigationDestinationProcessor : BaseProcessor() {

    private val destinations = mutableListOf<Element>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NavigationDestination::class.java.name
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        destinations += roundEnv.getElementsAnnotatedWith(NavigationDestination::class.java)
            .map {
                it.also(::generateDestinationForClass)
                it.also(::generateDestinationForFunction)
            }
        return false
    }

    private fun generateDestinationForClass(element: Element) {
        if (element.kind != ElementKind.CLASS) return
        val annotation = element.getAnnotation(NavigationDestination::class.java)

        val keyType = processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })

        val bindingName = element.getElementName()
            .replace(".", "_")
            .let { "_${it}_GeneratedNavigationBinding" }

        val classBuilder = TypeSpec.classBuilder(bindingName)
            .addOriginatingElement(element)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassNames.navigationComponentBuilderCommand)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination",
                        CodeBlock.of("\"${element.getElementName()}\"")
                    )
                    .addMember("navigationKey", CodeBlock.of("\"${keyType.getElementName()}\""))
                    .build()
            )
            .addGeneratedAnnotation()
            .addMethod(
                MethodSpec.methodBuilder("execute")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(
                        ParameterSpec
                            .builder(ClassNames.navigationComponentBuilder, "builder")
                            .build()
                    )
                    .addNavigationDestination(element, keyType)
                    .build()
            )
            .build()

        JavaFile
            .builder(EnroProcessor.GENERATED_PACKAGE, classBuilder)
            .addStaticImport(
                ClassNames.activityNavigationBindingKt,
                "createActivityNavigationBinding"
            )
            .addStaticImport(
                ClassNames.fragmentNavigationBindingKt,
                "createFragmentNavigationBinding"
            )
            .addStaticImport(
                ClassNames.syntheticNavigationBindingKt,
                "createSyntheticNavigationBinding"
            )
            .addStaticImport(ClassNames.jvmClassMappings, "getKotlinClass")
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun generateDestinationForFunction(element: Element) {
        if (element.kind != ElementKind.METHOD) return
        element as ExecutableElement

        element.annotationMirrors
            .firstOrNull {
                it.annotationType.asElement()
                    .getElementName() == "androidx.compose.runtime.Composable"
            }
            ?: run {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Function ${element.getElementName()} was marked as @NavigationDestination, but was not marked as @Composable")
                return
            }


        val isStatic = element.modifiers.contains(Modifier.STATIC)
        val parentIsObject = element.enclosingElement.enclosedElements.any { it.simpleName.toString() == "INSTANCE" }
        if(!isStatic && !parentIsObject) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Function ${element.getElementName()} is an instance function, which is not allowed.")
            return
        }

        val receiverTypes = element.kotlinReceiverTypes()
        val allowedReceiverTypes = listOf(
            "java.lang.Object",
            "dev.enro.core.compose.dialog.DialogDestination",
            "dev.enro.core.compose.dialog.BottomSheetDestination"
        )
        val isCompatibleReceiver = receiverTypes.all {
            allowedReceiverTypes.contains(it)
        }

        val hasNoParameters = element.parameters.size == 0
        val hasAllowedParameters = element.parameters.filter { !it.simpleName.startsWith("\$this") }.all {
            false
        }

        val parametersAreValid = (hasNoParameters || hasAllowedParameters) && isCompatibleReceiver
        if(!parametersAreValid) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Function ${element.getElementName()} has parameters which is not allowed.")
            return
        }

        val annotation = element.getAnnotation(NavigationDestination::class.java)
        val keyType =
            processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })

        val composableWrapper = createComposableWrapper(element, keyType)

        val bindingName = element.getElementName()
            .replace(".", "_")
            .let { "${it}_GeneratedNavigationBinding" }

        val classBuilder = TypeSpec.classBuilder(bindingName)
            .addOriginatingElement(element)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassNames.navigationComponentBuilderCommand)
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination",
                        CodeBlock.of("\"${EnroProcessor.GENERATED_PACKAGE}.$bindingName\"")
                    )
                    .addMember(
                        "navigationKey",
                        CodeBlock.of("\"${keyType.getElementName()}\"")
                    )
                    .build()
            )
            .addGeneratedAnnotation()
            .addMethod(
                MethodSpec.methodBuilder("execute")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(
                        ParameterSpec
                            .builder(ClassNames.navigationComponentBuilder, "builder")
                            .build()
                    )
                    .addStatement(
                        CodeBlock.of(
                            """
                                builder.binding(
                                    createComposableNavigationBinding(
                                        $1T.class,
                                        $composableWrapper.class
                                    )
                                )
                            """.trimIndent(),
                            keyType
                        )
                    )
                    .build()
            )
            .build()

        JavaFile
            .builder(EnroProcessor.GENERATED_PACKAGE, classBuilder)
            .addStaticImport(
                ClassNames.activityNavigationBindingKt,
                "createActivityNavigationBinding"
            )
            .addStaticImport(
                ClassNames.fragmentNavigationBindingKt,
                "createFragmentNavigationBinding"
            )
            .addStaticImport(
                ClassNames.syntheticNavigationBindingKt,
                "createSyntheticNavigationBinding"
            )
            .addStaticImport(
                ClassNames.composeNavigationBindingKt,
                "createComposableNavigationBinding"
            )
            .addStaticImport(ClassNames.jvmClassMappings, "getKotlinClass")
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun MethodSpec.Builder.addNavigationDestination(
        destination: Element,
        key: Element
    ): MethodSpec.Builder {
        val destinationName = destination.simpleName

        val destinationIsActivity = destination.extends(ClassNames.componentActivity)
        val destinationIsFragment = destination.extends(ClassNames.fragment)
        val destinationIsSynthetic = destination.implements(ClassNames.syntheticDestination)

        val annotation = destination.getAnnotation(NavigationDestination::class.java)

        addStatement(
            when {
                destinationIsActivity -> CodeBlock.of(
                    """
                    builder.binding(
                        createActivityNavigationBinding(
                            $1T.class,
                            $2T.class
                        )
                    )
                """.trimIndent(),
                    key,
                    destination
                )

                destinationIsFragment -> CodeBlock.of(
                    """
                    builder.binding(
                        createFragmentNavigationBinding(
                            $1T.class,
                            $2T.class
                        )
                    )
                """.trimIndent(),
                    key,
                    destination
                )

                destinationIsSynthetic -> CodeBlock.of(
                    """
                    builder.binding(
                        createSyntheticNavigationBinding(
                            $1T.class,
                            () -> new $2T()
                        )
                    )
                """.trimIndent(),
                    key,
                    destination
                )
                else -> {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "$destinationName does not extend Fragment, FragmentActivity, or SyntheticDestination")
                    CodeBlock.of("""
                        // Error: $destinationName does not extend Fragment, FragmentActivity, or SyntheticDestination
                    """.trimIndent())
                }
            }
        )

        return this
    }

    private fun createComposableWrapper(
        element: ExecutableElement,
        keyType: Element
    ): String {
        val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
        val composableWrapperName =
            element.getElementName().split(".").last() + "Destination"

        val receiverTypes = element.kotlinReceiverTypes()
        val additionalInterfaces = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.core.compose.dialog.DialogDestination" -> "DialogDestination"
                "dev.enro.core.compose.dialog.BottomSheetDestination" -> "BottomSheetDestination"
                else -> null
            }
        }.joinToString(separator = "") { ", $it" }

        val typeParameter = if(element.typeParameters.isEmpty()) "" else "<$composableWrapperName>"

        val additionalImports = receiverTypes.flatMap {
            when (it) {
                "dev.enro.core.compose.dialog.DialogDestination" -> listOf(
                    "dev.enro.core.compose.dialog.DialogDestination",
                    "dev.enro.core.compose.dialog.DialogConfiguration"
                )
                "dev.enro.core.compose.dialog.BottomSheetDestination" -> listOf(
                    "dev.enro.core.compose.dialog.BottomSheetDestination",
                    "dev.enro.core.compose.dialog.BottomSheetConfiguration",
                    "androidx.compose.material.ExperimentalMaterialApi"
                )
                else -> emptyList()
            }
        }.joinToString(separator = "") { "\n                import $it" }

        val additionalAnnotations = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.core.compose.dialog.BottomSheetDestination" ->
                    """
                        @OptIn(ExperimentalMaterialApi::class)
                    """.trimIndent()
                else -> null
            }
        }.joinToString(separator = "") { "\n                  $it" }

        val additionalBody = receiverTypes.mapNotNull {
            when (it) {
                "dev.enro.core.compose.dialog.DialogDestination" ->
                    """
                        override val dialogConfiguration: DialogConfiguration = DialogConfiguration()
                    """.trimIndent()
                "dev.enro.core.compose.dialog.BottomSheetDestination" ->
                    """
                        override val bottomSheetConfiguration: BottomSheetConfiguration = BottomSheetConfiguration()
                    """.trimIndent()
                else -> null
            }
        }.joinToString(separator = "") { "\n                    $it" }

        processingEnv.filer
            .createResource(
                StandardLocation.SOURCE_OUTPUT,
                EnroProcessor.GENERATED_PACKAGE,
                "$composableWrapperName.kt",
                element
            )
            .openWriter()
            .append(
                """
                package $packageName
                
                import androidx.compose.runtime.Composable
                import dev.enro.annotations.NavigationDestination
                import javax.annotation.Generated 
                $additionalImports
                
                import ${element.getElementName()}
                import ${ClassNames.composableDestination}
                import ${keyType.getElementName()}
                
                $additionalAnnotations
                @Generated("dev.enro.processor.NavigationDestinationProcessor")
                public class $composableWrapperName : ComposableDestination()$additionalInterfaces {
                    $additionalBody
                    
                    @Composable
                    override fun Render() {
                        ${element.simpleName}$typeParameter()
                    }
                }
                """.trimIndent()
            )
            .close()

        return "$packageName.$composableWrapperName"
    }
}