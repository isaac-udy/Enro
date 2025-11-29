package dev.enro.compiler.fir.generators

import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.fir.Keys
import dev.enro.compiler.fir.isFromGeneratedDeclaration
import dev.enro.compiler.fir.utils.EnroFirBuilder
import dev.enro.compiler.utils.buildPrintlnFunctionCall
import dev.enro.compiler.utils.nameForSymbol
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.declarations.declaredFunctions
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
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
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.ConstantValueKind

class NavigationBindingGenerator(
    session: FirSession,
    private val logger: EnroLogger,
) : FirDeclarationGenerationExtension(session) {
    private val builder = EnroFirBuilder(session)

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

        val function = createMemberFunction(
            owner = context.owner,
            key = Keys.GeneratedNavigationBinding,
            name = callableId.callableName,
            returnType = session.builtinTypes.unitType.coneType,
        ) {
            valueParameter(
                name = Name.identifier("scope"),
                type = EnroNames.Runtime.navigationModuleBuilderScope.constructClassLikeType()
            )
        }

        // Get the scope parameter symbol for use in function body generation
        val scopeParameterSymbol = function.valueParameters.first().symbol

        function.apply {
            when (navigationDestinationInformation.symbol) {
                is FirClassLikeSymbol -> replaceBody(
                    createClassBindingFor(
                        navigationDestinationInformation
                    )
                )

                is FirFunctionSymbol -> replaceBody(
                    createFunctionBindingFor(
                        navigationDestinationInformation,
                        scopeParameterSymbol
                    )
                )

                is FirPropertySymbol -> replaceBody(
                    createPropertyBindingFor(
                        navigationDestinationInformation,
                        scopeParameterSymbol
                    )
                )

                else -> error("Unsupported symbol type")
            }
        }

        return function.symbol
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

    @OptIn(SymbolInternals::class)
    private fun createFunctionBindingFor(
        navigationDestination: NavigationDestinationInformation,
        scopeParameter: FirValueParameterSymbol,
    ): FirBlock {
        val functionSymbol = navigationDestination.symbol as? FirNamedFunctionSymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a function")

        // Verify the function is annotated with @Composable
        val composableAnnotation = functionSymbol
            .getAnnotationByClassId(EnroNames.Compose.composableAnnotation, session)

        if (composableAnnotation == null) {
            error(
                "Function ${navigationDestination.packageName}.${navigationDestination.declarationName} " +
                        "is annotated with @NavigationDestination but is not a @Composable function. " +
                        "Only @Composable functions can be used as navigation destinations."
            )
        }

        val navigationKeyClassId = navigationDestination.getNavigationKeyName(session)
            ?: error("Could not find navigation key for ${navigationDestination.declarationName}")

        // Build: scope.destination(navigationDestination<KeyType> { ComposableFunctionName() })
        val destinationCall = buildComposableDestinationCall(
            scopeParameter = scopeParameter,
            navigationKeyClassId = navigationKeyClassId,
            composableFunctionSymbol = functionSymbol,
        )

        return buildBlock {
            statements += destinationCall
        }
    }

    /**
     * Builds: scope.destination(navigationDestination<KeyType> { ComposableFunctionName() })
     */
    @OptIn(SymbolInternals::class)
    private fun buildComposableDestinationCall(
        scopeParameter: FirValueParameterSymbol,
        navigationKeyClassId: ClassId,
        composableFunctionSymbol: FirNamedFunctionSymbol,
    ): FirFunctionCall {
        // Find the navigationDestination function
        val builderDestinationCallableId = EnroNames.Runtime.NavigationModuleBuilderScope.destinationFunction
        val builderSymbol = session.symbolProvider.getClassLikeSymbolByClassId(
            EnroNames.Runtime.navigationModuleBuilderScope
        ) as FirRegularClassSymbol

        val builderDestinationSymbol = builderSymbol.declaredFunctions(session)
            .filter {
                it.callableId == builderDestinationCallableId
            }
            .firstOrNull { it.valueParameterSymbols.size == 2 }
            ?: error("Could not find navigationDestination function symbol")

        val builderDestinationParameter = builderDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("destination") }

        val builderDestinationReference = buildResolvedNamedReference {
            source = null
            name = builderDestinationCallableId.callableName
            // CRITICAL: Link the reference to the actual function symbol
            resolvedSymbol = builderDestinationSymbol
        }

        // Find the navigationDestination function
        val navigationDestinationCallableId = CallableId(
            packageName = FqName("dev.enro.ui"),
            callableName = Name.identifier("navigationDestination")
        )

        val navigationDestinationSymbol = session.symbolProvider
            .getTopLevelFunctionSymbols(
                navigationDestinationCallableId.packageName,
                navigationDestinationCallableId.callableName
            )
            .firstOrNull { it.valueParameterSymbols.size == 2 }
            ?: error("Could not find navigationDestination function symbol")

        val navigationDestinationReference = buildResolvedNamedReference {
            source = null
            name = navigationDestinationCallableId.callableName
            // CRITICAL: Link the reference to the actual function symbol
            resolvedSymbol = navigationDestinationSymbol
        }

        val contentParameterSymbol = navigationDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("content") }

        // Create the type argument for NavigationKey
        val navigationKeyType = navigationKeyClassId.constructClassLikeType()

        val contentParameterLambda = builder.buildLambdaExpression(
            functionType = contentParameterSymbol.fir.returnTypeRef.coneType,
            returnType = session.builtinTypes.unitType,
            receiverType = EnroNames.Runtime.Ui.navigationDestinationScope.constructClassLikeType(
                arrayOf(navigationKeyType),
            ),
        ) {
            annotations += builder.buildComposableAnnotationCall(symbol)
            // Set the body
            body = buildBlock {
                // This property access expression is important to bind the anonymous function
                // to the outer function scope, otherwise the function anonymous function
                // gets added to the ComposableSingletons and doesn't appear to end up in the
                // *actual* compiled files, and will throw runtime errors because it can't be found
                statements += buildPropertyAccessExpression {
                    source = null
                    calleeReference = buildResolvedNamedReference {
                        source = null
                        name = scopeParameter.name
                        resolvedSymbol = scopeParameter
                    }
                    coneTypeOrNull = scopeParameter.resolvedReturnType
                }
                statements += buildFunctionCall {
                    source = composableFunctionSymbol.source
                    calleeReference = buildResolvedNamedReference {
                        source = null
                        name = composableFunctionSymbol.name
                        resolvedSymbol = composableFunctionSymbol
                    }
                    coneTypeOrNull = composableFunctionSymbol.resolvedReturnType
                    argumentList = buildResolvedArgumentList(original = null, mapping = linkedMapOf())
                }
            }
        }

        // Build the navigationDestination<KeyType> { ... } call
        val navigationDestinationCall = buildFunctionCall {
            source = null
            calleeReference = navigationDestinationReference
            coneTypeOrNull = EnroNames.Runtime.Ui.navigationDestinationProvider
                .constructClassLikeType(arrayOf(navigationKeyType))

            argumentList = buildResolvedArgumentList(
                original = null,
                mapping = linkedMapOf(
                    contentParameterLambda to contentParameterSymbol.fir,
                ),
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
        }

        return buildFunctionCall {
            source = null
            calleeReference = builderDestinationReference
            coneTypeOrNull = builderDestinationSymbol.resolvedReturnType
            dispatchReceiver = buildPropertyAccessExpression {
                source = null
                calleeReference = buildResolvedNamedReference {
                    source = null
                    name = scopeParameter.name
                    resolvedSymbol = scopeParameter
                }
                coneTypeOrNull = scopeParameter.resolvedReturnType
            }
            argumentList = buildResolvedArgumentList(
                original = null,
                mapping = linkedMapOf(
                    navigationDestinationCall to builderDestinationParameter.fir,
                )
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
        }
    }

    private fun createPropertyBindingFor(
        navigationDestination: NavigationDestinationInformation,
        scopeParameter: FirValueParameterSymbol,
    ): FirBlock {
        val symbol = navigationDestination.symbol as? FirPropertySymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a property")

        val navigationKeyClassId = navigationDestination.getNavigationKeyName(session)
            ?: error("Could not find navigation key for ${navigationDestination.declarationName}")

        val destinationCall = buildPropertyDestinationCall(
            scopeParameter = scopeParameter,
            navigationKeyClassId = navigationKeyClassId,
            propertySymbol = symbol,
        )

        return buildBlock {
            statements += destinationCall
        }
    }

    /**
     * Builds: scope.destination(property)
     */
    @OptIn(SymbolInternals::class)
    private fun buildPropertyDestinationCall(
        scopeParameter: FirValueParameterSymbol,
        navigationKeyClassId: ClassId,
        propertySymbol: FirPropertySymbol,
    ): FirFunctionCall {
        // Find the navigationDestination function
        val builderDestinationCallableId =
            EnroNames.Runtime.NavigationModuleBuilderScope.destinationFunction
        val builderSymbol = session.symbolProvider.getClassLikeSymbolByClassId(
            EnroNames.Runtime.navigationModuleBuilderScope
        ) as FirRegularClassSymbol

        val builderDestinationSymbol = builderSymbol.declaredFunctions(session)
            .filter {
                it.callableId == builderDestinationCallableId
            }
            .firstOrNull { it.valueParameterSymbols.size == 2 }
            ?: error("Could not find navigationDestination function symbol")

        val builderDestinationParameter = builderDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("destination") }

        val builderDestinationReference = buildResolvedNamedReference {
            source = null
            name = builderDestinationCallableId.callableName
            // CRITICAL: Link the reference to the actual function symbol
            resolvedSymbol = builderDestinationSymbol
        }

        val propertyAccess = buildPropertyAccessExpression {
            source = null
            calleeReference = buildResolvedNamedReference {
                source = null
                name = propertySymbol.name
                resolvedSymbol = propertySymbol
            }
            coneTypeOrNull = propertySymbol.resolvedReturnType
        }

        // Create the type argument for NavigationKey
        val navigationKeyType = navigationKeyClassId.constructClassLikeType()

        return buildFunctionCall {
            source = null
            calleeReference = builderDestinationReference
            coneTypeOrNull = builderDestinationSymbol.resolvedReturnType
            dispatchReceiver = buildPropertyAccessExpression {
                source = null
                calleeReference = buildResolvedNamedReference {
                    source = null
                    name = scopeParameter.name
                    resolvedSymbol = scopeParameter
                }
                coneTypeOrNull = scopeParameter.resolvedReturnType
            }
            argumentList = buildResolvedArgumentList(
                original = null,
                mapping = linkedMapOf(
                    propertyAccess to builderDestinationParameter.fir,
                )
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
        }
    }
}
