package dev.enro.compiler.fir.generators

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.fir.Keys
import dev.enro.compiler.fir.isFromGeneratedDeclaration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.typeResolver
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.ConstantValueKind

class NavigationBindingGenerator(
    session: FirSession,
    private val logger: EnroLogger,
) : FirDeclarationGenerationExtension(session) {
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(annotated(EnroNames.Annotations.navigationDestination.asSingleFqName()))
    }

    private val symbols: FirCache<Unit, Map<ClassId, NavigationDestinationInformation>, FirSession> =
        session.firCachesFactory.createCache { _, session ->
            session.predicateBasedProvider
                .getSymbolsByPredicate(annotated(EnroNames.Annotations.navigationDestination.asSingleFqName()))
                .associate { symbol ->
                    val info = NavigationDestinationInformation(symbol)
                    info.generatedBindingId to info
                }
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
        val destinationInformation = symbols.getValue(Unit, session)[classId]
        if (destinationInformation == null) {
            logger.warn(
                "Could not find originating declaration for classId: $classId",
            )
            return null
        }
        return createTopLevelClass(
            classId = classId,
            key = Keys.GeneratedNavigationBinding,
            classKind = ClassKind.OBJECT,
        ).apply {
            val navigationKey = destinationInformation.getNavigationKeyName(session)
            replaceAnnotations(annotations + buildAnnotation {
                annotationTypeRef = (session
                    .getRegularClassSymbolByClassId(EnroNames.Annotations.generatedNavigationBinding) as FirRegularClassSymbol
                ).defaultType().toFirResolvedTypeRef()
                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[Name.identifier("destination")] =
                        buildLiteralExpression(
                            source = null,
                            kind = ConstantValueKind.String,
                            value = "${destinationInformation.packageName.asString()}.${destinationInformation.declarationName}",
                            annotations = null,
                            setType = true,
                            prefix = null,
                        )
                    mapping[Name.identifier("navigationKey")] =
                        buildLiteralExpression(
                            source = null,
                            kind = ConstantValueKind.String,
                            value = navigationKey!!.asString(),
                            annotations = null,
                            setType = true,
                            prefix = null,
                        )
                    mapping[Name.identifier("bindingType")] =
                        buildLiteralExpression(
                            source = null,
                            kind = ConstantValueKind.Int,
                            value = when {
                                destinationInformation.symbol is FirClassLikeSymbol -> 0
                                destinationInformation.symbol is FirFunctionSymbol -> 1
                                destinationInformation.symbol is FirPropertySymbol -> 2
                                else -> error("Unsupported symbol type ${destinationInformation.symbol::class}")
                            },
                            annotations = null,
                            setType = true,
                            prefix = null,
                        )
                }
            })
        }.symbol
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        if (!classSymbol.origin.isFromGeneratedDeclaration(Keys.GeneratedNavigationBinding)) {
            return emptySet()
        }
        return setOf(
            SpecialNames.INIT,
            Name.identifier("bind"),
        )
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        if (!context.owner.origin.isFromGeneratedDeclaration(Keys.GeneratedNavigationBinding)) {
            return emptyList()
        }
        return listOf(
            createDefaultPrivateConstructor(
                context.owner,
                Keys.GeneratedNavigationBinding
            ).symbol
        )
    }

    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        return when {
            callableId == EnroNames.Generated.bindFunction -> {
                symbols.getValue(Unit, session).values.map { info ->
                    createTopLevelFunction(
                        key = Keys.GeneratedNavigationBinding,
                        callableId = callableId,
                        returnType = session.builtinTypes.unitType.coneType,
                    ){
                        valueParameter(
                            Name.identifier("binding"),
                            info.generatedBindingId.constructClassLikeType(),
                        )
                    }.symbol
                }
            }
            callableId.callableName.identifier == "bind" -> {
                requireNotNull(context)
                listOf(
                    createMemberFunction(
                        owner = context.owner,
                        key = Keys.GeneratedNavigationBinding,
                        name = callableId.callableName,
                        returnType = session.builtinTypes.unitType.coneType,
                    ) {
                        valueParameter(
                            name = Name.identifier("scope"),
                            type = EnroNames.Runtime.navigationModuleBuilderScope.constructClassLikeType()
                        )
                    } .symbol
                )
            }
            else -> emptyList()
        }
    }

    private class NavigationDestinationInformation(
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

        fun getNavigationDestinationAnnotation(session: FirSession) = symbol
            .getAnnotationByClassId(EnroNames.Annotations.navigationDestination, session)
            ?: error("No navigation destination annotation found for ${packageName}.${declarationName}")

        fun getNavigationKeyName(session: FirSession) =
            getNavigationDestinationAnnotation(session)
                .getKClassArgument(Name.identifier("key"), session)
                ?.classId
                ?.asSingleFqName()
//                ?: error("No navigation destination annotation found for ${packageName}.${declarationName}")
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