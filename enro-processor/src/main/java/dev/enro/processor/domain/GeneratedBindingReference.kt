package dev.enro.processor.domain

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import dev.enro.annotations.GeneratedNavigationBinding

data class GeneratedBindingReference(
    val qualifiedName: String,
    val destination: String,
    val navigationKey: String,
    val isPlatformOverride: Boolean,
    val isComposableFunction: Boolean,
    val containingFile: KSFile?,
) {
    companion object {
        @OptIn(KspExperimental::class)
        fun fromDeclaration(binding: KSClassDeclaration): GeneratedBindingReference {
            val bindingAnnotation = binding.getAnnotationsByType(GeneratedNavigationBinding::class).first()
            return GeneratedBindingReference(
                qualifiedName = binding.qualifiedName!!.asString(),
                destination = bindingAnnotation.destination,
                navigationKey = bindingAnnotation.navigationKey,
                isPlatformOverride = bindingAnnotation.isPlatformOverride,
                isComposableFunction = bindingAnnotation.isComposableFunction,
                containingFile = binding.containingFile,
            )
        }
    }
}
