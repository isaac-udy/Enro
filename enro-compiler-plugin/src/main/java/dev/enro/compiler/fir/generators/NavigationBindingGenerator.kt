package dev.enro.compiler.fir.generators

import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.fir.Keys
import dev.enro.compiler.fir.isFromGeneratedDeclaration
import dev.enro.compiler.utils.buildPrintlnFunctionCall
import dev.enro.compiler.utils.nameForSymbol
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
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
        return setOf(EnroNames.Generated.bindingReferenceFunction)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        val destinationInformation = symbols.getValue(Unit, session)[classId]
        if (destinationInformation == null) {
            logger.error(
                "Could not find originating declaration for classId: $classId",
            )
            return null
        }
        logger.warn("Generating top-level class for classId: $classId")
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
                            value = "${destinationInformation.packageName.asString().replace('.', '/')}/${destinationInformation.declarationName}",
                            annotations = null,
                            setType = true,
                            prefix = null,
                        )
                    mapping[Name.identifier("navigationKey")] =
                        buildLiteralExpression(
                            source = null,
                            kind = ConstantValueKind.String,
                            value = navigationKey!!.toString(),
                            annotations = null,
                            setType = true,
                            prefix = null,
                        )
                    mapping[Name.identifier("bindingType")] =
                        buildLiteralExpression(
                            source = null,
                            kind = ConstantValueKind.Int,
                            value = when {
                                destinationInformation.symbol is FirClassLikeSymbol -> GeneratedNavigationBinding.BindingType.CLASS
                                destinationInformation.symbol is FirFunctionSymbol -> GeneratedNavigationBinding.BindingType.FUNCTION
                                destinationInformation.symbol is FirPropertySymbol -> GeneratedNavigationBinding.BindingType.PROPERTY
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
            EnroNames.Generated.bindFunction(classSymbol.classId).callableName,
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

    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class, SymbolInternals::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        generateBindingReferenceFunction(
            callableId = callableId,
        )?.let { return it }

        generateBindFunction(
            callableId = callableId,
            context = context,
        )?.let { return listOf(it) }

        return emptyList()
    }


    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
    private fun generateBindingReferenceFunction(
        callableId: CallableId,
    ): List<FirNamedFunctionSymbol>? {
        if (callableId != EnroNames.Generated.bindingReferenceFunction) return null
        return symbols.getValue(Unit, session).values.map { info ->
            createTopLevelFunction(
                key = Keys.GeneratedNavigationBinding,
                callableId = callableId,
                returnType = session.builtinTypes.unitType.coneType,
            ){
                valueParameter(
                    Name.identifier("binding"),
                    info.generatedBindingId.constructClassLikeType(),
                )
            }.apply {
                // We're going to set the binding reference function to have
                // an empty block body, otherwise we'd need to configure this during IR
                replaceBody(buildBlock {  })
            }.symbol
        }
    }

    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
    private fun generateBindFunction(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): FirNamedFunctionSymbol? {
        if (context == null) return null
        val classId = callableId.classId ?: return null
        val bindFunctionId = EnroNames.Generated.bindFunction(classId)
        if (callableId != bindFunctionId) return null

        val navigationDestinationInformation = symbols.getValue(Unit, session)[classId]
            ?: error("No navigation destination information found for $classId")

        return createMemberFunction(
            owner = context.owner,
            key = Keys.GeneratedNavigationBinding,
            name = callableId.callableName,
            returnType = session.builtinTypes.unitType.coneType,
        ) {
            valueParameter(
                name = Name.identifier("scope"),
                type = EnroNames.Runtime.navigationModuleBuilderScope.constructClassLikeType()
            )
        }.apply {
            when (navigationDestinationInformation.symbol) {
                is FirClassLikeSymbol -> replaceBody(createClassBindingFor(navigationDestinationInformation))
                is FirFunctionSymbol -> replaceBody(createFunctionBindingFor(navigationDestinationInformation))
                is FirPropertySymbol -> replaceBody(createPropertyBindingFor(navigationDestinationInformation))
                else -> error("Unsupported symbol type")
            }
        }.symbol
    }

    private fun createClassBindingFor(
        navigationDestination: NavigationDestinationInformation
    ): FirBlock {
        val symbol = navigationDestination.symbol as? FirClassLikeSymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a class or object")

        return buildBlock {
            statements += buildPrintlnFunctionCall(
                session = session,
                value = "Class binding for ${nameForSymbol(symbol)} / ${navigationDestination.getNavigationKeyName(session)}",
            )
        }
    }

    private fun createFunctionBindingFor(
        navigationDestination: NavigationDestinationInformation
    ): FirBlock {
        val symbol = navigationDestination.symbol as? FirFunctionSymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a function")
        return buildBlock {
            statements += buildPrintlnFunctionCall(
                session = session,
                value = "Function binding for ${nameForSymbol(symbol)} / ${navigationDestination.getNavigationKeyName(session)}",
            )
        }
    }

    private fun createPropertyBindingFor(
        navigationDestination: NavigationDestinationInformation
    ): FirBlock {
        val symbol = navigationDestination.symbol as? FirPropertySymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a property")
        return buildBlock {
            statements += buildPrintlnFunctionCall(
                session = session,
                value = "Property binding for ${nameForSymbol(symbol)} / ${navigationDestination.getNavigationKeyName(session)}",
            )
        }
    }
}
