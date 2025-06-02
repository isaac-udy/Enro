package dev.enro3.processor.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.annotations.GeneratedNavigationModule
import dev.enro.processor.domain.GeneratedBindingReference
import dev.enro.processor.extensions.EnroLocation
import dev.enro.processor.extensions.getNamesFromKClasses

@OptIn(KspExperimental::class)
class GeneratedModuleReference(
    val resolver: Resolver,
    val declaration: KSClassDeclaration,
) {
    val qualifiedName: String by lazy {
        requireNotNull(declaration.qualifiedName).asString()
    }

    val bindings: List<GeneratedBindingReference> by lazy {
        val annotation = declaration.getAnnotationsByType(GeneratedNavigationModule::class).first()
        val bindings = getNamesFromKClasses { annotation.bindings }

        bindings.mapNotNull { bindingName ->
            val binding = requireNotNull(resolver.getClassDeclarationByName(bindingName))
            val bindingAnnotation = binding.getAnnotationsByType(GeneratedNavigationBinding::class).first()
            val isEnro3 = binding.superTypes.any {
                it.resolve().declaration.qualifiedName?.asString() == "dev.enro3.controller.NavigationModuleAction"
            }
            if (!isEnro3) return@mapNotNull null

            GeneratedBindingReference.Kotlin(
                binding = bindingName,
                destination = bindingAnnotation.destination,
                navigationKey = bindingAnnotation.navigationKey
            )
        }
    }

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

    companion object {
        fun load(resolver: Resolver): List<GeneratedModuleReference> {
            return resolver.getDeclarationsFromPackage(EnroLocation.GENERATED_PACKAGE)
                .filterIsInstance<KSClassDeclaration>()
                .filter { declaration ->
                    declaration.isAnnotationPresent(GeneratedNavigationModule::class)
                }
                .map { declaration ->
                    GeneratedModuleReference(
                        resolver = resolver,
                        declaration = declaration
                    )
                }
                .toList()
        }
    }
}