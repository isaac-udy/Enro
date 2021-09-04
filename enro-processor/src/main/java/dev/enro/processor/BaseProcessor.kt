package dev.enro.processor

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import javax.annotation.Generated
import javax.annotation.processing.AbstractProcessor
import javax.lang.model.element.Element
import javax.lang.model.element.QualifiedNameable

abstract class BaseProcessor : AbstractProcessor() {

    internal fun Element.getElementName(): String {
        val packageName = processingEnv.elementUtils.getPackageOf(this).toString()
        return if (this is QualifiedNameable) {
            qualifiedName.toString()
        } else {
            "$packageName.$simpleName"
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
}