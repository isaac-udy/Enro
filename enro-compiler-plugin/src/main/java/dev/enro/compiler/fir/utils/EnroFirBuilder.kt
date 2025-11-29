package dev.enro.compiler.fir.utils

import dev.enro.compiler.EnroNames
import dev.enro.compiler.fir.Keys
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.builder.FirAnonymousFunctionBuilder
import org.jetbrains.kotlin.fir.declarations.builder.buildAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildReceiverParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.expressions.builder.buildAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirReceiverParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType

class EnroFirBuilder(
    private val session: FirSession,
) {
    fun buildLambdaExpression(
        functionType: ConeKotlinType,
        returnType: FirTypeRef,
        receiverType: ConeKotlinType? = null,
        block: FirAnonymousFunctionBuilder.() -> Unit,
    ): FirAnonymousFunctionExpression {
        val anonymousFunctionSymbol = FirAnonymousFunctionSymbol()
        val anonymousFunction = buildAnonymousFunction {
            source = null
            moduleData = session.moduleData
            origin = FirDeclarationOrigin.Plugin(Keys.GeneratedNavigationBinding)
            typeRef = buildResolvedTypeRef { coneType = functionType }
            returnTypeRef = returnType
            receiverParameter = receiverType?.let {
                buildReceiverParameter {
                    symbol = FirReceiverParameterSymbol()
                    origin = FirDeclarationOrigin.Plugin(Keys.GeneratedNavigationBinding)
                    moduleData = session.moduleData
                    typeRef = buildResolvedTypeRef {
                        coneType = receiverType
                    }
                    containingDeclarationSymbol = anonymousFunctionSymbol
                }
            }
            this.valueParameters += valueParameters
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Local,
                Modality.FINAL,
                EffectiveVisibility.Local
            ).apply {
                this.isOperator = false
                this.isSuspend = false
                this.hasStableParameterNames = false
            }

            symbol = anonymousFunctionSymbol
            isLambda = true
            hasExplicitParameterList = false

            this.annotations += annotations
            this@buildAnonymousFunction.block()
        }

        return buildAnonymousFunctionExpression {
            source = null
            this.anonymousFunction = anonymousFunction
            isTrailingLambda = false
        }
    }

    fun buildComposableAnnotationCall(
        containingDeclarationSymbol: FirBasedSymbol<*>,
    ) : FirAnnotationCall {
        return buildAnnotationCall {
           this.containingDeclarationSymbol = containingDeclarationSymbol
            annotationTypeRef = buildResolvedTypeRef {
                coneType = EnroNames.Compose.composableAnnotation.constructClassLikeType()
            }
            calleeReference = buildResolvedNamedReference {
                name = EnroNames.Compose.composableAnnotation.shortClassName
                resolvedSymbol = session.symbolProvider.getClassLikeSymbolByClassId(EnroNames.Compose.composableAnnotation)
                    ?: error("Could not find Composable annotation symbol")
            }
        }
    }
}