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
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
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
        val destinationName = element.simpleName
        val destinationPackage = processingEnv.elementUtils.getPackageOf(element).toString()
        val annotation = element.getAnnotation(NavigationDestination::class.java)

        val keyType =
            processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })
        val keyName = keyType.simpleName
        val keyPackage = processingEnv.elementUtils.getPackageOf(keyType).toString()

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
                        CodeBlock.of("\"$destinationPackage.$destinationName\"")
                    )
                    .addMember("navigationKey", CodeBlock.of("\"$keyPackage.$keyName\""))
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
            .addStaticImport(ClassNames.activityNavigatorKt, "createActivityNavigator")
            .addStaticImport(ClassNames.fragmentNavigatorKt, "createFragmentNavigator")
            .addStaticImport(ClassNames.syntheticNavigatorKt, "createSyntheticNavigator")
            .addStaticImport(ClassNames.jvmClassMappings, "getKotlinClass")
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun generateDestinationForFunction(element: Element) {
        if (element.kind != ElementKind.METHOD) return
        element.annotationMirrors
            .firstOrNull {
                it.annotationType.asElement()
                    .getElementName() == "androidx.compose.runtime.Composable"
            }
            ?: throw java.lang.IllegalStateException("Function ${element.getElementName()} was marked as @NavigationDestination, but was not marked as @Composable")

        val annotation = element.getAnnotation(NavigationDestination::class.java)
        val keyType =
            processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })
        val keyName = keyType.simpleName
        val keyPackage = processingEnv.elementUtils.getPackageOf(keyType).toString()

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
                        CodeBlock.of("\"$keyPackage.$keyName\"")
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
                                builder.navigator(
                                    createComposableNavigator(
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
            .addStaticImport(ClassNames.activityNavigatorKt, "createActivityNavigator")
            .addStaticImport(ClassNames.fragmentNavigatorKt, "createFragmentNavigator")
            .addStaticImport(ClassNames.syntheticNavigatorKt, "createSyntheticNavigator")
            .addStaticImport(ClassNames.composeNavigatorKt, "createComposableNavigator")
            .addStaticImport(ClassNames.jvmClassMappings, "getKotlinClass")
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun MethodSpec.Builder.addNavigationDestination(
        destination: Element,
        key: Element
    ): MethodSpec.Builder {
        val destinationName = destination.simpleName

        val destinationIsActivity = destination.extends(ClassNames.fragmentActivity)
        val destinationIsFragment = destination.extends(ClassNames.fragment)
        val destinationIsSynthetic = destination.implements(ClassNames.syntheticDestination)

        val annotation = destination.getAnnotation(NavigationDestination::class.java)

        addStatement(
            when {
                destinationIsActivity -> CodeBlock.of(
                    """
                    builder.navigator(
                        createActivityNavigator(
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
                    builder.navigator(
                        createFragmentNavigator(
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
                    builder.navigator(
                        createSyntheticNavigator(
                            $1T.class,
                            new $2T()
                        )
                    )
                """.trimIndent(),
                    key,
                    destination
                )
                else -> {
                    throw IllegalStateException("$destinationName does not extend Fragment, FragmentActivity, or SyntheticDestination")
                }
            }
        )

        return this
    }

    private fun createComposableWrapper(
        element: Element,
        keyType: Element
    ): String {
        val composableWrapperName =
            element.getElementName().replace(".", "_") + "_ComposableDestination"

        processingEnv.filer
            .createResource(
                StandardLocation.SOURCE_OUTPUT,
                EnroProcessor.GENERATED_PACKAGE,
                "$composableWrapperName.kt"
            )
            .openWriter()
            .append(
                """
                package ${EnroProcessor.GENERATED_PACKAGE}
                
                import ${element.getElementName()}
                import ${ClassNames.composableDestination}
                import androidx.compose.runtime.Composable
                import dev.enro.annotations.NavigationDestination
                import ${keyType.getElementName()}
                
                class $composableWrapperName : ComposableDestination() {
                    @Composable
                    override fun Render() {
                        ${element.simpleName}()
                    }
                }
                """.trimIndent()
            )
            .close()

        return composableWrapperName
    }
}