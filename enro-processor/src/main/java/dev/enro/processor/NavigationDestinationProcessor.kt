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
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

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
                it.also(::generateDestination)
            }
        return false
    }

    private fun generateDestination(element: Element) {
        val destinationName = element.simpleName
        val destinationPackage = processingEnv.elementUtils.getPackageOf(element).toString()
        val annotation = element.getAnnotation(NavigationDestination::class.java)

        val keyType =
            processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })
        val keyName = keyType.simpleName
        val keyPackage = processingEnv.elementUtils.getPackageOf(keyType).toString()

        val bindingName = element.getElementName()
                .replace(".","_")
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
}