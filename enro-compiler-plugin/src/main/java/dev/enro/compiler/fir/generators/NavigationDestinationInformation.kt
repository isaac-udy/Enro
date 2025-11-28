package dev.enro.compiler.fir.generators

import dev.enro.compiler.EnroNames
import dev.enro.compiler.utils.nameForSymbol
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

class NavigationDestinationInformation(
    val symbol: FirBasedSymbol<*>,
) {
    val packageName = symbol.packageFqName()
    val declarationName = nameForSymbol(symbol)
        ?: error("$symbol is not a valid NavigationDestination")

    val generatedBindingId = ClassId(
        packageFqName = EnroNames.Generated.generatedPackage,
        topLevelName = Name.identifier(
            "_$packageName.${declarationName}Binding".replace(
                ".",
                "_"
            )
        ),
    )

    fun getNavigationDestinationAnnotation(session: FirSession): FirAnnotation {
        return symbol
            .getAnnotationByClassId(EnroNames.Annotations.navigationDestination, session)
            ?: error("No navigation destination annotation found for ${packageName}.${declarationName}")
    }

    fun getNavigationKeyName(session: FirSession): ClassId? {
        return getNavigationDestinationAnnotation(session)
            .getKClassArgument(Name.identifier("key"), session)
            ?.classId
    }
}