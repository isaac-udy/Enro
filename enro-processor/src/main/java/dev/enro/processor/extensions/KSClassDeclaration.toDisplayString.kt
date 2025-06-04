package dev.enro.processor.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration

fun KSClassDeclaration?.toDisplayString(): String {
    if (this == null) return "null"
    val qualifiedName = qualifiedName?.asString() ?: return simpleName.asString()
    val packageName = packageName.asString()
    return qualifiedName.removePrefix("$packageName.")
}