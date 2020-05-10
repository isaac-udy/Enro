package nav.enro.processor

import com.squareup.kotlinpoet.*
import nav.enro.annotations.NavigationDestination
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.lang.IllegalStateException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KClass

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class EnroProcessor : AbstractProcessor() {
    private lateinit var messager: Messager
    private lateinit var typeUtils: Types
    private lateinit var elementUtils: Elements

    val services = mutableListOf<String>()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        typeUtils = processingEnv.typeUtils
        elementUtils = processingEnv.elementUtils
    }

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf(IncrementalAnnotationProcessorType.ISOLATING.processorOption)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(NavigationDestination::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(NavigationDestination::class.java).forEach {
                when {
                    it.kind != ElementKind.CLASS -> processingEnv.messager
                        .printMessage(Diagnostic.Kind.ERROR, "NavigationDestination is only valid for classes")

                    !it.extends("androidx.fragment.app.FragmentActivity")
                            &&  !it.extends("androidx.fragment.app.Fragment") -> processingEnv.messager
                        .printMessage(Diagnostic.Kind.ERROR, "NavigationDestination is only valid for on classes that extend either Fragment or FragmentActivity")

                    !elementUtils.getTypeElement(
                        getName { it.getAnnotation(NavigationDestination::class.java).fromKey }
                    ).extends("nav.enro.core.NavigationKey") ->  processingEnv.messager
                        .printMessage(Diagnostic.Kind.ERROR, "NavigationDestination is only valid with a fromType that extends NavigationKey")

                    else -> {
                        services.add(process(it))
                    }
                }
            }

        if(roundEnv.processingOver()) {
            val serviceDeclaration = "META-INF/services/nav.enro.core.controller.NavigationControllerBuilderAction"
            processingEnv.filer
                .createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    serviceDeclaration,
                    *services.map { elementUtils.getTypeElement(it) }.toTypedArray()
                )
                .openWriter()
                .apply {
                    services.forEach {
                        write("$it\n")
                    }
                }
                .close()
        }

        return false
    }


    private fun Element.extends(superName: String): Boolean {
        val typeMirror = processingEnv.elementUtils.getTypeElement(superName).asType()
        return typeUtils.isSubtype(asType(), typeMirror)
    }

    private fun process(element: Element): String {
        val destinationIsActivity = element.extends("androidx.fragment.app.FragmentActivity")
        val destinationIsFragment = element.extends("androidx.fragment.app.Fragment")

        val destinationName = element.simpleName
        val destinationPackage = elementUtils.getPackageOf(element).toString()
        val fileName = "${element.simpleName}Destination"

        val keyType = elementUtils.getTypeElement(getName {
                element.getAnnotation(NavigationDestination::class.java).fromKey
        })
        val keyName = keyType.simpleName
        val keyPackage = elementUtils.getPackageOf(keyType).toString()

        val interfaceType = ClassName("nav.enro.core.controller","NavigationControllerBuilderAction")
        val controllerBuilderType = ClassName("nav.enro.core.controller","NavigationControllerBuilder")

        val classBuilder = TypeSpec.classBuilder(fileName)
            .addOriginatingElement(element)
            .addSuperinterface(interfaceType)
            .addFunction(
                FunSpec.builder("apply")
                    .addParameter("builder", controllerBuilderType)
                    .returns(ClassName("kotlin", "Unit"))
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement(
                        when {
                            destinationIsActivity -> "builder.activityNavigator<$keyName, $destinationName>()"
                            destinationIsFragment ->  "builder.fragmentNavigator<$keyName, $destinationName>()"
                            else -> throw IllegalStateException()
                        }
                    )
                    .build()
            )
            .build()

        val file = FileSpec.builder(destinationPackage, fileName)
            .addType(classBuilder)
            .build()

        file.writeTo(processingEnv.filer)
        return "$destinationPackage.$fileName"
    }

    fun getName(block: () -> KClass<*>) : String {
        try {
            return block().java.name
        }
        catch (ex: MirroredTypeException) {
            return ex.typeMirror.asTypeName().toString()
        }
    }

}