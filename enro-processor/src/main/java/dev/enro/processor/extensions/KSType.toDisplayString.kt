package dev.enro.processor.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

fun KSType?.toDisplayString(): String {
    return when (val declaration = this?.declaration) {
        is KSClassDeclaration -> declaration.toDisplayString()
        else -> toString()
    }
}