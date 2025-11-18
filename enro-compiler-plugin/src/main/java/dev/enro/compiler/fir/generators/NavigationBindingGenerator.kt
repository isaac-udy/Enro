package dev.enro.compiler.fir.generators

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.fir.Keys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind
import kotlin.random.Random

class NavigationBindingGenerator(
    session: FirSession,
    private val logger: EnroLogger,
) : FirDeclarationGenerationExtension(session) {
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(annotated(EnroNames.Annotations.navigationDestination.asSingleFqName()))
    }

    private val symbols: FirCache<Unit, Map<ClassId, FirBasedSymbol<*>>, FirSession> =
        session.firCachesFactory.createCache { _, _ ->
            session.predicateBasedProvider
                .getSymbolsByPredicate(annotated(EnroNames.Annotations.navigationDestination.asSingleFqName()))
                .mapNotNull { symbol ->
                    val packageName = symbol.packageFqName()
                    val declarationName = nameForSymbol(symbol)
                        ?: return@mapNotNull null

                    val generatedBindingId = ClassId(
                        packageFqName = EnroNames.Generated.generatedPackage,
                        topLevelName = Name.identifier(
                            "_$packageName.${declarationName}Binding".replace(
                                ".",
                                "_"
                            )
                        ),
                    )
                    generatedBindingId to symbol
                }
                .toMap()
        }


    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelClassIds(): Set<ClassId> {
        return symbols.getValue(Unit, session).keys
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        return setOf(EnroNames.Generated.bindFunction)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        logger.warn(
            "Starting FirBasedSymbol generation for classId: $classId",
        )
        val symbol = symbols.getValue(Unit, session)[classId]
        if (symbol == null) {
            logger.warn(
                "Could not find originating declaration for classId: $classId",
            )
            return null
        }
        val navigationDestinationAnnotation = symbol
            .getAnnotationByClassId(EnroNames.Annotations.navigationDestination, session)

        if (navigationDestinationAnnotation == null) {
            logger.warn(
                "Could not find @NavigationDestination annotation on originating declaration for classId: $classId",
            )
            return null
        }

        val navigationKey = navigationDestinationAnnotation
            .getKClassArgument(Name.identifier("key"), session)
            ?.classId
            ?.asSingleFqName()
        if (navigationKey == null) {
            logger.warn(
                "Could not find navigation key in @NavigationDestination annotation on originating declaration for classId: $classId",
            )
            return null
        }

        logger.warn(
            "Generating navigation binding: destination = ${nameForSymbol(symbol)} and key = ${navigationKey.asString()}",
        )
        return createTopLevelClass(
            classId = classId,
            key = Keys.GeneratedNavigationBinding,
            classKind = ClassKind.OBJECT,
        ).apply {
            createConstructor(
                owner = this@apply.symbol,
                key = Keys.GeneratedNavigationBinding,
            )
        }.symbol
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        require(callableId == EnroNames.Generated.bindFunction)

        symbols.getValue(Unit, session).values.forEach { classId ->
            logger.warn(
                "Generating navigation binding FUNCTION for $classId",
            )
        }
        return listOf()
    }
}

private fun nameForSymbol(symbol: FirBasedSymbol<*>): String? {
    return when (symbol) {
        is FirClassLikeSymbol -> symbol.name.asString()
        is FirPropertySymbol -> symbol.name.asString()
        is FirFunctionSymbol -> symbol.name.asString()
        else -> null
    }
}