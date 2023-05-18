package dev.enro.processor.extensions

import com.squareup.javapoet.ClassName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

internal fun Element.implements(
    processingEnv: ProcessingEnvironment,
    className: ClassName
): Boolean {
    if (this !is TypeElement) return false
    val typeMirror = processingEnv.typeUtils.erasure(
        processingEnv.elementUtils.getTypeElement(
            className.canonicalName()
        ).asType()
    )
    return processingEnv.typeUtils.isAssignable(asType(), typeMirror)
}
