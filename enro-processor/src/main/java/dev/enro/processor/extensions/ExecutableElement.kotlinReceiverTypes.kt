package dev.enro.processor.extensions

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement


fun ExecutableElement.kotlinReceiverTypes(processingEnv: ProcessingEnvironment): List<String> {
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