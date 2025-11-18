package dev.enro.compiler.utils

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.resolve.fqName

fun FirStatement.getAnnotation(
    session: FirSession,
    annotationName: String,
): FirAnnotation? {
    return annotations.firstOrNull {
        it.fqName(session)?.asString() == annotationName
    }
}

inline fun <reified T: Annotation> FirStatement.getAnnotation(
    session: FirSession,
): FirAnnotation? {
    val annotationName = T::class.qualifiedName ?: return null
    return annotations.firstOrNull {
        it.fqName(session)?.asString() == annotationName
    }
}