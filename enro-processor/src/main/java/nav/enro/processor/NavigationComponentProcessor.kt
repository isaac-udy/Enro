package nav.enro.processor

import com.squareup.kotlinpoet.*
import nav.enro.annotations.GeneratedNavigationBinding
import nav.enro.annotations.NavigationComponent
import nav.enro.annotations.NavigationDestination
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class NavigationComponentProcessor : BaseProcessor() {

    private val components = mutableListOf<Element>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NavigationComponent::class.java.name,
            NavigationDestination::class.java.name,
            GeneratedNavigationBinding::class.java.name
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
        if (roundEnv.processingOver()) {
            components.forEach { generateComponent(it) }
        }
        return false
    }

    private fun generateComponent(component: Element) {
        val destinations =
            processingEnv.elementUtils
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
            .apply {
                addOriginatingElement(component)
                destinations.forEach {
                    addOriginatingElement(it.aggregate)
                }
                addModifiers(KModifier.INTERNAL)
                addSuperinterface(EnroProcessor.builderActionType)
                addFunction(
                    FunSpec.builder("execute")
                        .addParameter("builder", EnroProcessor.builderType)
                        .returns(ClassName("kotlin", "Unit"))
                        .addModifiers(KModifier.OVERRIDE).apply {
                            destinations.forEach {
                                addNavigationDestination(it)
                            }
                        }
                        .build()
                )
            }
            .build()

        val file = FileSpec
            .builder(
                processingEnv.elementUtils.getPackageOf(component).toString(),
                generatedName
            )
            .apply {
                destinations.forEach {
                    addImport(it.aggregate.getElementName(), "")
                }
            }
            .addType(classBuilder)
            .build()

        file.writeTo(processingEnv.filer)
    }

    private fun FunSpec.Builder.addNavigationDestination(navigationDestination: NavigationDestinationArguments) {
        addStatement(
            "${navigationDestination.aggregate.simpleName}().execute(builder)"
        )
    }
}
data class NavigationDestinationArguments(
    val aggregate: Element,
    val destination: Element,
    val navigationKey: Element
)