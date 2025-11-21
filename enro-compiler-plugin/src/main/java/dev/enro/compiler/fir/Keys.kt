package dev.enro.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin

object Keys {
    data object GeneratedNavigationBinding : GeneratedDeclarationKey() {
        override fun toString() = "GeneratedNavigationBinding"
    }
}

fun FirDeclarationOrigin.isFromGeneratedDeclaration(
    key: GeneratedDeclarationKey,
): Boolean {
    return this == FirDeclarationOrigin.Plugin(key)
}