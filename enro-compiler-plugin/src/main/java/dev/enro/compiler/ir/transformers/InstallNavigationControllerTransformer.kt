package dev.enro.compiler.ir.transformers

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.ir.EnroSymbols
import dev.enro.compiler.ir.createIrBuilder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturnableBlock
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

class InstallNavigationControllerTransformer(
    private val pluginContext: IrPluginContext,
    private val enroSymbols: EnroSymbols,
    private val logger: EnroLogger,
) : IrElementTransformerVoid() {

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitCall(expression: IrCall): IrExpression {
        val callee = expression.symbol.owner
        if (callee.symbol != enroSymbols.installNavigationController) return expression
        val application = expression.arguments[0]!!

        val controllerType = pluginContext.referenceClass(EnroNames.Runtime.enroController)!!
        return pluginContext.createIrBuilder(expression.symbol).irReturnableBlock(
            resultType = controllerType.defaultType
        ) {
            // Create the internalCreateEnroController call
            val createControllerCall = irCall(enroSymbols.internalCreateEnroController).apply {
                val builderScopeClass = pluginContext.referenceClass(
                    EnroNames.Runtime.navigationModuleBuilderScope
                )?.owner ?: error("NavigationModule.BuilderScope not found")

                val lambdaParent = parent
                val bindFunctions = enroSymbols.bindingReferenceFunctions
                arguments[0] = with(pluginContext) {
                    irLambda(
                        parent = lambdaParent,
                        returnType = symbols.unit.defaultType,
                        valueParameters = listOf(builderScopeClass.defaultType),
                        content = { lambda ->
                            // Add bind calls for each binding function
                            bindFunctions.forEach { bindFunction ->
                                val bindingObjectType = bindFunction.owner.parameters[0].type.classOrFail
                                val bindingObjectClassId = bindingObjectType.owner.classId
                                    ?: error("Couldn't find classId for $bindingObjectType")
                                val bindingFunctionId = EnroNames.Generated.bindFunction(bindingObjectClassId)
                                val bindMethod = bindingObjectType.functions.single {
                                    bindingFunctionId == it.owner.callableId
                                }

                                +irCall(bindMethod).apply {
                                    arguments[0] = irGetObject(bindingObjectType)
                                    arguments[1] = irGet(lambda.parameters[0])
                                }
                            }
                        }
                    )
                }
            }
            val controllerVal = irTemporary(
                value = createControllerCall, // Initialize the variable with the function call
                nameHint = "controller",
                isMutable = false
            )
            val installFunction = controllerType.functions.singleOrNull {
                it.owner.name.asString() == "install"
            } ?: error("EnroController.install() function not found")

            +irCall(installFunction).apply {
                // The controller instance is the dispatch receiver for the instance method
                arguments[0] = irGet(controllerVal)
                arguments[1] = application
            }
            +irGet(controllerVal)
        }
    }
}

// From
// https://kotlinlang.slack.com/archives/C7L3JB43G/p1672258639333069?thread_ts=1672258597.659509&cid=C7L3JB43G
context(context: IrPluginContext)
internal fun irLambda(
    parent: IrDeclarationParent,
    valueParameters: List<IrType>,
    returnType: IrType,
    suspend: Boolean = false,
    content: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
): IrFunctionExpression {
    val lambda =
        context.irFactory
            .buildFun {
                startOffset = SYNTHETIC_OFFSET
                endOffset = SYNTHETIC_OFFSET
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
                name = Name.special("<anonymous>")
                visibility = DescriptorVisibilities.LOCAL
                isSuspend = suspend
                this.returnType = returnType
            }
            .apply {
                this.parent = parent
                valueParameters.forEachIndexed { index, type -> addValueParameter("arg$index", type) }
                body = context.createIrBuilder(this.symbol).irBlockBody { content(this@apply) }
            }
    return IrFunctionExpressionImpl(
        startOffset = SYNTHETIC_OFFSET,
        endOffset = SYNTHETIC_OFFSET,
        type =
            run {
                when (suspend) {
                    false -> context.irBuiltIns.functionN(valueParameters.size)
                    else -> context.irBuiltIns.suspendFunctionN(valueParameters.size)
                }.typeWith(*valueParameters.toTypedArray(), returnType)
            },
        origin = IrStatementOrigin.LAMBDA,
        function = lambda,
    )
}
