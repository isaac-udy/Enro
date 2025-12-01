package dev.enro.compiler.fir.generators

import dev.enro.compiler.EnroNames
import dev.enro.compiler.utils.nameForSymbol
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

class NavigationDestinationInformation(
    val symbol: FirBasedSymbol<*>,
    val isPlatformOverride: Boolean,
) {
    val packageName = symbol.packageFqName()
    val declarationName = nameForSymbol(symbol)
        ?: error("$symbol is not a valid NavigationDestination")

    val generatedBindingId = Name.identifier(
        "_$packageName.${declarationName}".replace(
            ".",
            "_"
        )
    )

    fun getNavigationKeyName(session: FirSession): ClassId? {
        val annotation = when (isPlatformOverride) {
            true -> symbol.getAnnotationByClassId(
                classId = EnroNames.Annotations.navigationDestinationPlatformOverride,
                session = session
            )

            else -> symbol.getAnnotationByClassId(
                classId = EnroNames.Annotations.navigationDestination,
                session = session
            )
        }
        requireNotNull(annotation) { "$symbol is not a valid NavigationDestination or NavigationDestination.PlatformOverride" }
        return annotation
            .getKClassArgument(Name.identifier("key"), session)
            ?.classId
    }
}