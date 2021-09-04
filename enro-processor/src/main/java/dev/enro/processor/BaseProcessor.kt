package dev.enro.processor

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import javax.annotation.Generated
import javax.annotation.processing.AbstractProcessor
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.QualifiedNameable
import javax.tools.Diagnostic

abstract class BaseProcessor : AbstractProcessor() {

    internal fun Element.getElementName(): String {
        val packageName = processingEnv.elementUtils.getPackageOf(this).toString()
        return when (this) {
            is QualifiedNameable -> {
                qualifiedName.toString()
            }
            is ExecutableElement -> {
                val kotlinMetadata = enclosingElement.getAnnotation(Metadata::class.java)
                when (kotlinMetadata?.kind) {
                    // metadata kind 1 is a "class" type, which means this method belongs to a
                    // class or object, rather than being a top-level file function (kind 2)
                    1 -> "${enclosingElement.getElementName()}.$simpleName"
                    else -> "$packageName.$simpleName"
                }
            }
            else -> {
                "$packageName.$simpleName"
            }
        }
    }

    internal fun Element.extends(className: ClassName): Boolean {
        val typeMirror = className.asElement().asType()
        return processingEnv.typeUtils.isSubtype(asType(), typeMirror)
    }

    internal fun Element.implements(className: ClassName): Boolean {
        val typeMirror = processingEnv.typeUtils.erasure(className.asElement().asType())
        return processingEnv.typeUtils.isAssignable(asType(), typeMirror)
    }

    internal fun ClassName.asElement() = processingEnv.elementUtils.getTypeElement(canonicalName())

    internal fun TypeSpec.Builder.addGeneratedAnnotation(): TypeSpec.Builder {
        addAnnotation(
            AnnotationSpec.builder(Generated::class.java)
                .addMember("value", "\"${this@BaseProcessor::class.java.name}\"")
                .build()
        )
        return this
    }


    fun ExecutableElement.kotlinReceiverTypes(): List<String> {
        val receiver = parameters.firstOrNull {
            it.simpleName.startsWith("\$this")
        } ?: return emptyList()

        val typeParameterNames = typeParameters.map { it.simpleName.toString() }
        val superTypes = processingEnv.typeUtils.directSupertypes(receiver.asType()).map { it.toString() }
        val receiverTypeName = receiver.asType().toString()

        return if(typeParameterNames.contains(receiverTypeName)) {
            superTypes
        } else {
            superTypes + receiverTypeName
        }
    }
}