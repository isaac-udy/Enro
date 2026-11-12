package dev.enro.processor.extensions

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.QualifiedNameable

internal fun Element.getElementName(processingEnv: ProcessingEnvironment): String {
    val packageName = processingEnv.elementUtils.getPackageOf(this).toString()
    return when (this) {
        is QualifiedNameable -> {
            qualifiedName.toString()
        }
        is ExecutableElement -> {
            val kotlinMetadata = enclosingElement.getAnnotation(Metadata::class.java)
            when (kotlinMetadata?.kind) {
                // Metadata kind 1 is a class: the method is a member of a class or object.
                // Kind 2 is a file: the method is a top-level function.
                1 -> "${enclosingElement.getElementName(processingEnv)}.$simpleName"
                else -> "$packageName.$simpleName"
            }
        }
        else -> {
            "$packageName.$simpleName"
        }
    }
}