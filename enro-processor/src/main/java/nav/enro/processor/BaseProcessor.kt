package nav.enro.processor

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

abstract class BaseProcessor : AbstractProcessor() {

    internal fun Element.getElementName(): String {
        return processingEnv.elementUtils.getPackageOf(this).toString()+"."+this.simpleName
    }

    internal fun Element.getDestinationName(): String {
        val packageName = processingEnv.elementUtils
            .getPackageOf(this).toString()
            .replace(".","_")
        return "${packageName}_${this.simpleName}"
    }

    internal fun Element.extends(superName: String): Boolean {
        val typeMirror = processingEnv.elementUtils.getTypeElement(superName).asType()
        return processingEnv.typeUtils.isSubtype(asType(), typeMirror)
    }

    internal fun Element.implements(superName: String): Boolean {
        val typeMirror = processingEnv.typeUtils.erasure(processingEnv.elementUtils.getTypeElement(superName).asType())
        return processingEnv.typeUtils.isAssignable(asType(), typeMirror)
    }
}