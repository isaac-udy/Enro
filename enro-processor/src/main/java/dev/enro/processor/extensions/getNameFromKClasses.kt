package dev.enro.processor.extensions

import com.google.devtools.ksp.KSTypesNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.squareup.javapoet.ClassName
import javax.lang.model.type.MirroredTypesException
import kotlin.reflect.KClass

@OptIn(KspExperimental::class)
internal fun getNamesFromKClasses(block: () -> Array<KClass<*>>): List<String> {
    val exception = runCatching {
        block().map { it.java.name }
    }.exceptionOrNull()

    return when (exception) {
        is KSTypesNotPresentException -> {
            exception.ksTypes.map { type ->
                requireNotNull(type.declaration.qualifiedName).asString()
            }
        }
        is MirroredTypesException -> {
            exception.typeMirrors.map { typeMirror ->
                ClassName.get(typeMirror).toString()
            }
        }
        else -> emptyList()
    }
}