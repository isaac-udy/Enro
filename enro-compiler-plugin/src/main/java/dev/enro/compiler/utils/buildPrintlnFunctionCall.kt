package dev.enro.compiler.utils

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

@OptIn(SymbolInternals::class)
fun buildPrintlnFunctionCall(
    session: FirSession,
    value: String,
): FirFunctionCall {
    val printlnCallableId = CallableId(
        packageName = FqName("kotlin.io"),
        className = null,
        callableName = Name.identifier("println")
    )

    val printlnSymbol = session.symbolProvider
        .getTopLevelFunctionSymbols(printlnCallableId.packageName, printlnCallableId.callableName)
        .firstOrNull {
            it.valueParameterSymbols.size == 1
                    && it.valueParameterSymbols[0].name == Name.identifier("message")
        }
        ?: error("Could not find the symbol for kotlin.println")

    val resolvedReference = buildResolvedNamedReference {
        source = null
        name = printlnCallableId.callableName
        // CRITICAL: Link the reference to the actual function symbol
        resolvedSymbol = printlnSymbol
    }
    val messageArgument = buildLiteralExpression(
        source = null,
        // The type of the string literal is String
        kind = ConstantValueKind.String,
        value = value,
        setType = true
    )

    val arguments = buildResolvedArgumentList(
        original = null,
        mapping = linkedMapOf(
            messageArgument to printlnSymbol.valueParameterSymbols.first().fir
        )
    )

    return buildFunctionCall {
        source = null
        calleeReference = resolvedReference
        coneTypeOrNull = printlnSymbol.resolvedReturnType
        argumentList = arguments
    }
}