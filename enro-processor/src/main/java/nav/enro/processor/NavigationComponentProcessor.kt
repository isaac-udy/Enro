package nav.enro.processor

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import nav.enro.annotations.GeneratedNavigationBinding
import nav.enro.annotations.NavigationComponent
import nav.enro.annotations.NavigationDestination
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.Generated
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@AutoService(Processor::class)
class NavigationComponentProcessor : BaseProcessor() {

    private val components = mutableListOf<Element>()
    private val processed = mutableSetOf<String>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NavigationComponent::class.java.name
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        components += roundEnv.getElementsAnnotatedWith(NavigationComponent::class.java)

        val elementsToWaitFor = roundEnv.getElementsAnnotatedWith(NavigationComponent::class.java) +
                roundEnv.getElementsAnnotatedWith(GeneratedNavigationBinding::class.java) +
                roundEnv.getElementsAnnotatedWith(NavigationDestination::class.java)

        if (elementsToWaitFor.isEmpty()) {
            components.forEach { generateComponent(it) }
        }
        return false
    }

    private fun generateComponent(component: Element) {
        val name = ClassName.get(component as TypeElement).canonicalName()
        if (processed.contains(name)) return
        processed.add(name)

        val destinations = processingEnv.elementUtils
            .getPackageElement(EnroProcessor.GENERATED_PACKAGE)
            .enclosedElements
            .mapNotNull {
                val annotation = it.getAnnotation(GeneratedNavigationBinding::class.java)
                    ?: return@mapNotNull null

                NavigationDestinationArguments(
                    aggregate = it,
                    destination = processingEnv.elementUtils.getTypeElement(annotation.destination),
                    navigationKey = processingEnv.elementUtils.getTypeElement(annotation.navigationKey)
                )
            }

        val generatedName = "${component.simpleName}Navigation"
        val classBuilder = TypeSpec.classBuilder(generatedName)
            .addOriginatingElement(component)
            .addOriginatingElement(
                processingEnv.elementUtils
                    .getPackageElement(EnroProcessor.GENERATED_PACKAGE)
            )
            .apply {
                destinations.forEach {
                    addOriginatingElement(processingEnv.elementUtils.getPackageOf(it.destination))
                }
            }
            .addGeneratedAnnotation()
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassNames.navigationComponentBuilderCommand)
            .addMethod(
                MethodSpec.methodBuilder("execute")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(
                        ParameterSpec
                            .builder(ClassNames.navigationComponentBuilder, "builder")
                            .build()
                    )
                    .apply {
                        destinations.forEach {
                            addStatement(CodeBlock.of("new $1T().execute(builder)", it.aggregate))
                        }
                    }
                    .build()
            )
            .build()

        JavaFile
            .builder(
                processingEnv.elementUtils.getPackageOf(component).toString(),
                classBuilder
            )
            .build()
            .writeTo(processingEnv.filer)
    }
}

internal data class NavigationDestinationArguments(
    val aggregate: Element,
    val destination: Element,
    val navigationKey: Element
)