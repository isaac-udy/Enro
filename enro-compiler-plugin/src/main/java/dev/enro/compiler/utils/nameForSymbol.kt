package dev.enro.compiler.utils

import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

fun nameForSymbol(symbol: FirBasedSymbol<*>): String? {
    return when (symbol) {
        is FirClassLikeSymbol -> symbol.name.asString()
        is FirPropertySymbol -> symbol.name.asString()
        is FirFunctionSymbol -> symbol.name.asString()
        else -> null
    }
}