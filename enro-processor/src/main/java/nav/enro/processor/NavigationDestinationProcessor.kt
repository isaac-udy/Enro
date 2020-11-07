package nav.enro.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import nav.enro.annotations.NavigationDestination
import nav.enro.annotations.GeneratedNavigationBinding
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.lang.IllegalStateException
import javax.annotation.Generated
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

        val classBuilder = TypeSpec.classBuilder(element.getDestinationName())
            .addOriginatingElement(element)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
                ClassName.get(
                    "nav.enro.core.controller",
                    "NavigationComponentBuilderCommand"
                )
            )
            .addAnnotation(
                AnnotationSpec.builder(GeneratedNavigationBinding::class.java)
                    .addMember(
                        "destination",
                        CodeBlock.of("\"$destinationPackage.$destinationName\"")
                    )
                    .addMember("navigationKey", CodeBlock.of("\"$keyPackage.$keyName\""))
                    .build()
            )
            .addAnnotation(
                AnnotationSpec.builder(Generated::class.java)
                    .addMember("value", "\"${this::class.java.name}\"")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("execute")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(
                        ParameterSpec
                            .builder(
                                ClassName.get(
                                    "nav.enro.core.controller",
                                    "NavigationComponentBuilder"
                                ), "builder"
                            )
                            .build()
                    )
                    .addNavigationDestination(element, keyType)
                    .build()
            )
            .build()

        JavaFile
            .builder(EnroProcessor.GENERATED_PACKAGE, classBuilder)
            .addStaticImport(
                ClassName.get(
                    "nav.enro.core.navigator", "NavigatorDefinitionKt"
                ),
                "createActivityNavigator",
                "createFragmentNavigator",
                "createSyntheticNavigator"
            )
            .addStaticImport(ClassName.get("kotlin.jvm", "JvmClassMappingKt"), "getKotlinClass")
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun MethodSpec.Builder.addNavigationDestination(
        destination: Element,
        key: Element
    ): MethodSpec.Builder {
        val destinationName = destination.simpleName

        val destinationIsActivity = destination.extends("androidx.fragment.app.FragmentActivity")
        val destinationIsFragment = destination.extends("androidx.fragment.app.Fragment")
        val destinationIsSynthetic =
            destination.implements("nav.enro.core.navigator.SyntheticDestination")

        val annotation = destination.getAnnotation(NavigationDestination::class.java)

        addStatement(
            when {
                destinationIsActivity -> CodeBlock.of(
                    """
                    builder.add(
                        createActivityNavigator(
                            getKotlinClass($1T.class),
                            getKotlinClass($2T.class),
                            it -> {
                                ${if (annotation.allowDefault) "it.defaultKey(new $1T());" else ""}
                                return $3T.INSTANCE;
                            }
                        )
                    )
                """.trimIndent(),
                    key,
                    destination,
                    ClassName.get("kotlin", "Unit")
                )

                destinationIsFragment -> CodeBlock.of(
                    """
                    builder.add(
                        createFragmentNavigator(
                            getKotlinClass($1T.class),
                            getKotlinClass($2T.class),
                            it -> {
                                ${if (annotation.allowDefault) "it.defaultKey(new $1T());" else ""}
                                return $3T.INSTANCE;
                            }
                        )
                    )
                """.trimIndent(),
                    key,
                    destination,
                    ClassName.get("kotlin", "Unit")
                )

                destinationIsSynthetic -> CodeBlock.of(
                    """
                    builder.add(
                        createSyntheticNavigator(
                            getKotlinClass($1T.class),
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