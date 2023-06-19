package dev.enro.processor.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.GeneratedNavigationModule
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.getElementName
import dev.enro.processor.extensions.getNamesFromKClasses
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

@OptIn(KspExperimental::class)
sealed class GeneratedModuleReference {
    class Kotlin(
        val resolver: Resolver,
        val declaration: KSClassDeclaration
    ) : GeneratedModuleReference() {
        // containingFiles from references from other gradle modules will
        // return null here, so we're going to filter nulls here.
        // This means that sources may be an empty list, but that is expected in some cases.
        val sources: List<KSFile> = listOf(declaration.containingFile)
            .plus(
                bindings.map {
                    val bindingDeclaration = requireNotNull(resolver.getClassDeclarationByName(it.binding))
                    bindingDeclaration.containingFile
                }
            )
            .filterNotNull()
    }

    class Java(
        val processingEnvironment: ProcessingEnvironment,
        val element: Element
    ) : GeneratedModuleReference()

    val qualifiedName: String by lazy {
        when(this) {
            is Kotlin -> requireNotNull(declaration.qualifiedName).asString()
            is Java -> element.getElementName(processingEnvironment)
        }
    }

    val bindings: List<GeneratedBindingReference> by lazy {
        when (this) {
            is Kotlin -> {
                val annotation = declaration.getAnnotationsByType(GeneratedNavigationModule::class).first()
                val bindings = getNamesFromKClasses { annotation.bindings }

                bindings.map { bindingName ->
                    val binding = requireNotNull(resolver.getClassDeclarationByName(bindingName))
                    val bindingAnnotation = binding.getAnnotationsByType(GeneratedNavigationBinding::class).first()

                    GeneratedBindingReference.Kotlin(
                        binding = bindingName,
                        destination = bindingAnnotation.destination,
                        navigationKey = bindingAnnotation.navigationKey
                    )
                }
            }
            is Java -> {
                val annotation = element.getAnnotation(GeneratedNavigationModule::class.java)
                val bindings = getNamesFromKClasses { annotation.bindings }

                bindings.map { bindingName ->
                    val binding = processingEnvironment.elementUtils.getTypeElement(bindingName)
                    val bindingAnnotation = binding.getAnnotation(GeneratedNavigationBinding::class.java)

                    GeneratedBindingReference.Java(
                        binding = bindingName,
                        destination = bindingAnnotation.destination,
                        navigationKey = bindingAnnotation.navigationKey
                    )
                }
            }
        }
    }

    companion object {
        fun load(resolver: Resolver): List<Kotlin> {
            return resolver.getDeclarationsFromPackage(EnroLocation.GENERATED_PACKAGE)
                .filterIsInstance<KSClassDeclaration>()
                .filter { declaration ->
                    declaration.isAnnotationPresent(GeneratedNavigationModule::class)
                }
                .map { declaration ->
                    Kotlin(
                        resolver = resolver,
                        declaration = declaration
                    )
                }
                .toList()
        }

        fun load(processingEnvironment: ProcessingEnvironment) : List<Java> {
            return processingEnvironment.elementUtils
                .getPackageElement(EnroLocation.GENERATED_PACKAGE)
                .runCatching {
                    enclosedElements
                }
                .getOrNull()
                .orEmpty()
                .filter { element ->
                    element.getAnnotation(GeneratedNavigationModule::class.java) != null
                }
                .map { element ->
                    Java(
                        processingEnvironment = processingEnvironment,
                        element = element,
                    )
                }
        }
    }
}