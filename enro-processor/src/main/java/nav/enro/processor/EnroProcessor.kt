package nav.enro.processor

import com.squareup.kotlinpoet.*
import nav.enro.annotations.NavigationComponent
import nav.enro.annotations.NavigationDestination
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Completion
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KClass

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class EnroProcessor : AbstractProcessor() {

    private val components = mutableListOf<Element>()
    private val destinations = mutableListOf<Element>()

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf(IncrementalAnnotationProcessorType.ISOLATING.processorOption)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            NavigationDestination::class.java.name,
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
        destinations += roundEnv.getElementsAnnotatedWith(NavigationDestination::class.java)
            .map {
                it.also(::generateDestination)
            }

        components += roundEnv.getElementsAnnotatedWith(NavigationComponent::class.java)

        if (roundEnv.processingOver()) {
            components.forEach { generateComponent(it, destinations) }
        }
        return false
    }

    private fun generateDestination(element: Element) {
        val destinationIsActivity  = element.extends("androidx.fragment.app.FragmentActivity")
        val destinationIsFragment  = element.extends("androidx.fragment.app.Fragment")
        val destinationIsSynthetic = element.implements("nav.enro.core.navigator.SyntheticDestination")

        val destinationName = element.simpleName
        val destinationPackage = processingEnv.elementUtils.getPackageOf(element).toString()
        val fileName = "${element.simpleName}Destination"
        val annotation = element.getAnnotation(NavigationDestination::class.java)

        val keyType = processingEnv.elementUtils.getTypeElement(getNameFromKClass { annotation.key })
        val keyName = keyType.simpleName
        val keyPackage = processingEnv.elementUtils.getPackageOf(keyType).toString()

        val classBuilder = TypeSpec.classBuilder(getDestinationNameFor(element))
            .addOriginatingElement(element)
            .addSuperinterface(builderActionType)
            .addFunction(
                FunSpec.builder("execute")
                    .addParameter("builder", builderType)
                    .returns(ClassName("kotlin", "Unit"))
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement(
                        when {
                            destinationIsActivity -> """
|                               builder.activityNavigator<$keyName, $destinationName>{ 
                                    ${if (annotation.allowDefault) "defaultKey($keyName())" else ""}
                                }""".trimMargin()
                            destinationIsFragment -> """
|                               builder.fragmentNavigator<$keyName, $destinationName>{ 
                                    ${if (annotation.allowDefault) "defaultKey($keyName())" else ""}
                                }""".trimMargin()
                            destinationIsSynthetic -> """
                                builder.syntheticNavigator($destinationName())
                                """
                            else -> {
                                throw IllegalStateException("$destinationName does not extend Fragment, FragmentActivity, or SyntheticDestination")
                            }
                        }
                    )
                    .build()
            )
            .build()

        val file = FileSpec.builder(GENERATED_PACKAGE, fileName)
            .addImport(destinationPackage, element.simpleName.toString())
            .addImport(keyPackage, keyName.toString())
            .addType(classBuilder)
            .build()

        file.writeTo(processingEnv.filer)
    }

    private fun generateComponent(component: Element, generatedDestinations: List<Element>) {
        val generatedDestinationNames =  generatedDestinations.map {
            "$GENERATED_PACKAGE.${getDestinationNameFor(it)}"
        }

        val existingDestinationNames = kotlin.runCatching {
            processingEnv.elementUtils
                .getPackageElement(GENERATED_PACKAGE)
                .enclosedElements
                .mapNotNull {
                    processingEnv.getElementName(it)
                }
        }.getOrNull().orEmpty()

        val destinationNames = (generatedDestinationNames + existingDestinationNames).toSet()

        val generatedName = "${component.simpleName}Navigation"
        val classBuilder = TypeSpec.classBuilder(generatedName)
            .apply {
                addOriginatingElement(component)
                addModifiers(KModifier.INTERNAL)
                addSuperinterface(builderActionType)
                addFunction(
                    FunSpec.builder("execute")
                        .addParameter("builder", builderType)
                        .returns(ClassName("kotlin", "Unit"))
                        .addModifiers(KModifier.OVERRIDE)
                        .addStatement(
                            destinationNames.joinToString("\n") {
                                "$it().execute(builder)"
                            }
                        )
                        .build()
                )
            }
            .build()

        val file = FileSpec.builder(
            processingEnv.elementUtils.getPackageOf(component).toString(),
            generatedName
        ).addType(classBuilder).build()

        file.writeTo(processingEnv.filer)

        val serviceDeclaration = "META-INF/services/nav.enro.core.controller.NavigationComponentBuilderCommand"
        processingEnv.filer
            .createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                serviceDeclaration
            )
            .openWriter()
            .apply {
                destinationNames.forEach {
                    write("$it\n")
                }
            }
            .close()
    }

    private fun Element.extends(superName: String): Boolean {
        val typeMirror = processingEnv.elementUtils.getTypeElement(superName).asType()
        return processingEnv.typeUtils.isSubtype(asType(), typeMirror)
    }

    private fun Element.implements(superName: String): Boolean {
        val typeMirror = processingEnv.typeUtils.erasure(processingEnv.elementUtils.getTypeElement(superName).asType())
        return processingEnv.typeUtils.isAssignable(asType(), typeMirror)
    }

    private fun getDestinationNameFor(element: Element): String {
        val packageName = processingEnv.elementUtils
            .getPackageOf(element).toString()
            .replace(".","_")
        return "${packageName}_${element.simpleName}"
    }

    companion object {
        private const val GENERATED_PACKAGE = "nav.enro.generated"

        val builderActionType = ClassName("nav.enro.core.controller", "NavigationComponentBuilderCommand")
        val builderType = ClassName("nav.enro.core.controller", "NavigationComponentBuilder")
    }
}

fun ProcessingEnvironment.getElementName(element: Element): String {
    return elementUtils.getPackageOf(element).toString()+"."+element.simpleName
}

private fun getNameFromKClass(block: () -> KClass<*>) : String {
    try {
        return block().java.name
    }
    catch (ex: MirroredTypeException) {
        return ex.typeMirror.asTypeName().toString()
    }
}
