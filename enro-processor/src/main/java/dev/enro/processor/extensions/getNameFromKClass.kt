package dev.enro.processor.extensions

import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.squareup.javapoet.ClassName
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

@OptIn(KspExperimental::class)
internal fun getNameFromKClass(block: () -> KClass<*>) : String {
    val exception = runCatching {
        return block().java.name
    }.exceptionOrNull()

    return when (exception) {
        is KSTypeNotPresentException -> {
            requireNotNull(exception.ksType.declaration.qualifiedName).asString()
        }
        is MirroredTypeException -> {
            ClassName.get(exception.typeMirror).toString()
        }
        else -> throw exception!!//error("getNameFromKClass did not throw an exception as expected")
    }
}