package dev.enro.compiler.fir.generators

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.fir.Keys
import dev.enro.compiler.fir.utils.EnroFirBuilder
import dev.enro.compiler.utils.nameForSymbol
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.declaredFunctions
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedQualifier
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

@OptIn(SymbolInternals::class)
class NavigationBindingGenerator(
    session: FirSession,
    private val logger: EnroLogger,
) : FirDeclarationGenerationExtension(session) {
    private val builder = EnroFirBuilder(session)

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(annotated(EnroNames.Annotations.navigationDestination.asSingleFqName()))
        register(annotated(EnroNames.Annotations.navigationDestinationPlatformOverride.asSingleFqName()))
    }

    private val symbols: FirCache<Unit, Map<Name, NavigationDestinationInformation>, FirSession> =
        session.firCachesFactory.createCache { _, session ->
            val destinations = session.predicateBasedProvider
                .getSymbolsByPredicate(annotated(EnroNames.Annotations.navigationDestination.asSingleFqName()))
                .map {
                    NavigationDestinationInformation(
                        symbol = it,
                        isPlatformOverride = false
                    )
                }
            val platformOverrides = session.predicateBasedProvider
                .getSymbolsByPredicate(annotated(EnroNames.Annotations.navigationDestinationPlatformOverride.asSingleFqName()))
                .map {
                    NavigationDestinationInformation(
                        symbol = it,
                        isPlatformOverride = true
                    )
                }
            (destinations + platformOverrides)
                .associate { info ->
                    info.generatedBindingId to info
                }
        }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        val isGeneratedBinding = classSymbol.name.identifierOrNullIfSpecial
            .orEmpty()
            .startsWith("EnroBindings")
            .and(classSymbol.packageFqName() == EnroNames.Generated.generatedPackage)

        if (!isGeneratedBinding) {
            return emptySet()
        }

        // Add the top-level "bind" function that calls all individual bindings
        return setOf(Name.identifier("bind"))
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        // Handle the top-level "bind" function that calls all individual bindings
        generateTopLevelBindFunction(
            callableId = callableId,
            context = context,
        )?.let { return listOf(it) }
        return emptyList()
    }

    private fun generateTopLevelBindFunction(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): FirNamedFunctionSymbol? {
        if (context == null) return null
        val classId = callableId.classId ?: return null
        if (classId.packageFqName != EnroNames.Generated.generatedPackage) return null
        if (!classId.shortClassName.identifier.startsWith("EnroBindings")) return null
        if (callableId.callableName.identifier != "bind") return null

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

        val destinations = symbols.getValue(Unit, session).values
        val scopeParameterSymbol = function.valueParameters.first().symbol
        // Build the body that calls all individual bind functions
        function.replaceBody(
            buildBlock {
                statements += destinations.map { destination ->
                    generateDestinationBindingStatement(
                        destinationInformation = destination,
                        scopeParameter = scopeParameterSymbol,
                    )
                }
            }
        )

        return function.symbol
    }

    private fun generateDestinationBindingStatement(
        destinationInformation: NavigationDestinationInformation,
        scopeParameter: FirValueParameterSymbol,
    ): FirStatement {
        return when (destinationInformation.symbol) {
            is FirClassLikeSymbol -> createClassBindingFor(
                destinationInformation,
                scopeParameter
            )

            is FirFunctionSymbol -> createFunctionBindingFor(
                destinationInformation,
                scopeParameter
            )

            is FirPropertySymbol -> createPropertyBindingFor(
                destinationInformation,
                scopeParameter
            )

            else -> error("Unsupported symbol type")
        }
    }

    private fun createClassBindingFor(
        navigationDestination: NavigationDestinationInformation,
        scopeParameter: FirValueParameterSymbol,
    ): FirStatement {
        val symbol = navigationDestination.symbol as? FirClassLikeSymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a class or object")

        val isFragment = symbol.isSubclassOf(EnroNames.Android.fragment, session)
        val isActivity = symbol.isSubclassOf(EnroNames.Android.componentActivity, session)

        if (!isFragment && !isActivity) {
            error("${nameForSymbol(symbol)} must extend Fragment or ComponentActivity")
        }

        val navigationKeyClassId = navigationDestination.getNavigationKeyName(session)
            ?: error("Could not find navigation key for ${navigationDestination.declarationName}")

        val destinationCall = if (isFragment) {
            buildFragmentDestinationCall(
                scopeParameter = scopeParameter,
                navigationKeyClassId = navigationKeyClassId,
                classSymbol = symbol,
                isPlatformOverride = navigationDestination.isPlatformOverride,
            )
        } else {
            buildActivityDestinationCall(
                scopeParameter = scopeParameter,
                navigationKeyClassId = navigationKeyClassId,
                classSymbol = symbol,
                isPlatformOverride = navigationDestination.isPlatformOverride,
            )
        }

        return destinationCall
    }

    private fun FirClassLikeSymbol<*>.isSubclassOf(
        classId: ClassId,
        session: FirSession,
    ): Boolean {
        if (this.classId == classId) return true
        val firClass = fir as? org.jetbrains.kotlin.fir.declarations.FirClass ?: return false
        return firClass.superTypeRefs.any { typeRef: FirTypeRef ->
            val coneType = typeRef.coneTypeSafe<ConeClassLikeType>() ?: return@any false
            val symbol =
                session.symbolProvider.getClassLikeSymbolByClassId(coneType.lookupTag.classId)
                    ?: return@any false
            symbol.isSubclassOf(classId, session)
        }
    }

    @OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
    @Suppress("DirectDeclarationsAccess")
    private fun buildFragmentDestinationCall(
        scopeParameter: FirValueParameterSymbol,
        navigationKeyClassId: ClassId,
        classSymbol: FirClassLikeSymbol<*>,
        isPlatformOverride: Boolean,
    ): FirFunctionCall {
        // Find the destination function
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

        val platformOverrideParameter = builderDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("isPlatformOverride") }

        val builderDestinationReference = buildResolvedNamedReference {
            source = null
            name = builderDestinationCallableId.callableName
            resolvedSymbol = builderDestinationSymbol
        }

        // Find the fragmentDestination function
        val fragmentDestinationCallableId = EnroNames.Runtime.Ui.fragmentDestination
        val fragmentDestinationSymbol = session.symbolProvider
            .getTopLevelFunctionSymbols(
                fragmentDestinationCallableId.packageName,
                fragmentDestinationCallableId.callableName
            )
            .firstOrNull { it.valueParameterSymbols.size == 2 }
            ?: error("Could not find fragmentDestination function symbol")

        val fragmentDestinationReference = buildResolvedNamedReference {
            source = null
            name = fragmentDestinationCallableId.callableName
            resolvedSymbol = fragmentDestinationSymbol
        }

        val navigationKeyType = session.getRegularClassSymbolByClassId(navigationKeyClassId)!!.defaultType()

        val fragmentDestinationCall = buildFunctionCall {
            source = null
            calleeReference = fragmentDestinationReference
            coneTypeOrNull = EnroNames.Runtime.Ui.navigationDestinationProvider
                .constructClassLikeType(arrayOf(navigationKeyType))

            argumentList = buildResolvedArgumentList(
                original = null,
                mapping = linkedMapOf(),
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = classSymbol.defaultType() }
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
                    fragmentDestinationCall to builderDestinationParameter.fir,
                    buildLiteralExpression(
                        source = null,
                        // The type of the string literal is String
                        kind = ConstantValueKind.Boolean,
                        value = isPlatformOverride,
                        setType = true
                    ) to platformOverrideParameter.fir
                )
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
        }
    }

    @OptIn(SymbolInternals::class)
    private fun buildActivityDestinationCall(
        scopeParameter: FirValueParameterSymbol,
        navigationKeyClassId: ClassId,
        classSymbol: FirClassLikeSymbol<*>,
        isPlatformOverride: Boolean,
    ): FirFunctionCall {
        // Find the destination function
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

        val platformOverrideParameter = builderDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("isPlatformOverride") }

        val builderDestinationReference = buildResolvedNamedReference {
            source = null
            name = builderDestinationCallableId.callableName
            // CRITICAL: Link the reference to the actual function symbol
            resolvedSymbol = builderDestinationSymbol
        }

        // Find the activityDestination function
        val activityDestinationCallableId = EnroNames.Runtime.Ui.activityDestination
        val activityDestinationSymbol = session.symbolProvider
            .getTopLevelFunctionSymbols(
                activityDestinationCallableId.packageName,
                activityDestinationCallableId.callableName
            )
            .firstOrNull { it.valueParameterSymbols.isEmpty() }
            ?: error("Could not find activityDestination function symbol")

        val activityDestinationReference = buildResolvedNamedReference {
            source = null
            name = activityDestinationCallableId.callableName
            // CRITICAL: Link the reference to the actual function symbol
            resolvedSymbol = activityDestinationSymbol
        }

        val navigationKeyType = session.getRegularClassSymbolByClassId(navigationKeyClassId)!!.defaultType()

        val activityDestinationCall = buildFunctionCall {
            source = null
            calleeReference = activityDestinationReference
            coneTypeOrNull = EnroNames.Runtime.Ui.navigationDestinationProvider
                .constructClassLikeType(arrayOf(navigationKeyType))

            argumentList = buildResolvedArgumentList(
                original = null,
                mapping = linkedMapOf(),
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = classSymbol.defaultType() }
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
                    activityDestinationCall to builderDestinationParameter.fir,
                    buildLiteralExpression(
                        source = null,
                        // The type of the string literal is String
                        kind = ConstantValueKind.Boolean,
                        value = isPlatformOverride,
                        setType = true
                    ) to platformOverrideParameter.fir
                )
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
        }
    }

    @OptIn(SymbolInternals::class)
    private fun createFunctionBindingFor(
        navigationDestination: NavigationDestinationInformation,
        scopeParameter: FirValueParameterSymbol,
    ): FirStatement {
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
            isPlatformOverride = navigationDestination.isPlatformOverride
        )

        return destinationCall
    }

    /**
     * Builds: scope.destination(navigationDestination<KeyType> { ComposableFunctionName() })
     */
    @OptIn(SymbolInternals::class)
    private fun buildComposableDestinationCall(
        scopeParameter: FirValueParameterSymbol,
        navigationKeyClassId: ClassId,
        composableFunctionSymbol: FirNamedFunctionSymbol,
        isPlatformOverride: Boolean,
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

        val platformOverrideParameter = builderDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("isPlatformOverride") }

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
        val navigationKeyType = session.getRegularClassSymbolByClassId(navigationKeyClassId)!!.defaultType()

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
                    argumentList =
                        buildResolvedArgumentList(original = null, mapping = linkedMapOf())
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
                    buildLiteralExpression(
                        source = null,
                        // The type of the string literal is String
                        kind = ConstantValueKind.Boolean,
                        value = isPlatformOverride,
                        setType = true
                    ) to platformOverrideParameter.fir
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
    ): FirStatement {
        val symbol = navigationDestination.symbol as? FirPropertySymbol
            ?: error("${nameForSymbol(navigationDestination.symbol)} is not a property")

        val navigationKeyClassId = navigationDestination.getNavigationKeyName(session)
            ?: error("Could not find navigation key for ${navigationDestination.declarationName}")

        val destinationCall = buildPropertyDestinationCall(
            scopeParameter = scopeParameter,
            navigationKeyClassId = navigationKeyClassId,
            propertySymbol = symbol,
            isPlatformOverride = navigationDestination.isPlatformOverride,
        )

        return destinationCall
    }

    /**
     * Builds: scope.destination(property)
     */
    @OptIn(SymbolInternals::class)
    private fun buildPropertyDestinationCall(
        scopeParameter: FirValueParameterSymbol,
        navigationKeyClassId: ClassId,
        propertySymbol: FirPropertySymbol,
        isPlatformOverride: Boolean,
    ): FirStatement {
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

        val platformOverrideParameter = builderDestinationSymbol.valueParameterSymbols
            .first { it.name == Name.identifier("isPlatformOverride") }

        val builderDestinationReference = buildResolvedNamedReference {
            source = null
            name = builderDestinationCallableId.callableName
            // CRITICAL: Link the reference to the actual function symbol
            resolvedSymbol = builderDestinationSymbol
        }

        val dispatchReceiverExpression = propertySymbol.callableId?.classId?.let { classId ->
            val classSymbol =
                session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirRegularClassSymbol
                    ?: error("Could not find class symbol for $classId")

            if (classSymbol.classKind != ClassKind.OBJECT && classSymbol.classKind != ClassKind.ENUM_ENTRY) {
                error("Navigation destinations can only be properties of objects or top-level properties. ${classId.asString()} is a ${classSymbol.classKind}")
            }
            buildResolvedQualifier {
                packageFqName = classId.packageFqName
                relativeClassFqName = classId.relativeClassName
                symbol = classSymbol
                coneTypeOrNull = classSymbol.defaultType()
                resolvedToCompanionObject =
                    (classSymbol as? FirRegularClassSymbol)?.isCompanion == true
            }
        }

        val propertyAccess = buildPropertyAccessExpression {
            source = null
            calleeReference = buildResolvedNamedReference {
                source = null
                name = propertySymbol.name
                resolvedSymbol = propertySymbol
            }
            coneTypeOrNull = propertySymbol.resolvedReturnType
            this.dispatchReceiver = dispatchReceiverExpression
        }

        // Create the type argument for NavigationKey
        val navigationKeyType = session.getRegularClassSymbolByClassId(navigationKeyClassId)!!.defaultType()

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
                    buildLiteralExpression(
                        source = null,
                        // The type of the string literal is String
                        kind = ConstantValueKind.Boolean,
                        value = isPlatformOverride,
                        setType = true
                    ) to platformOverrideParameter.fir
                )
            )
            typeArguments += org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance {
                this.typeRef = buildResolvedTypeRef { coneType = navigationKeyType }
                this.variance = org.jetbrains.kotlin.types.Variance.INVARIANT
            }
        }
    }
}
