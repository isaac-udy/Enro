package dev.enro.compiler.ir.transformers

import dev.enro.annotations.GeneratedNavigationBinding
import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.ir.EnroSymbols
import dev.enro.compiler.ir.createIrBuilder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.getAnnotationArgumentValue
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@Suppress("ReplaceGetOrSet")
class BindFunctionTransformer(
    private val pluginContext: IrPluginContext,
    private val enroSymbols: EnroSymbols,
    private val logger: EnroLogger,
) : IrElementTransformerVoid() {
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val printlnFunction = pluginContext.referenceFunctions(
        CallableId(
            packageName = FqName("kotlin.io"),
            callableName = Name.identifier("println")
        )
    ).first {
        val params = it.owner.parameters
        params.size == 1 && params.single().type == pluginContext.irBuiltIns.anyNType
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        generateBindFunctionBody(declaration)?.let { return it }
        return declaration
    }

    private fun generateBindFunctionBody(declaration: IrFunction): IrStatement? {
        val parent = declaration.parent
        if (parent !is IrClass) return null
        if (parent.classId == null) return null
        if (declaration is IrConstructor) return null

        val bindFunctionId = EnroNames.Generated.bindFunction(parent.classId!!)
        if (bindFunctionId != declaration.callableId) return null

        val scope = declaration.parameters.first { it.name.identifierOrNullIfSpecial == "scope" }
        val bindingData = GeneratedNavigationBindingData(
            pluginContext= pluginContext,
            bindingClass = parent,
        )

        declaration.body = when (bindingData.bindingType) {
            GeneratedNavigationBinding.BindingType.Class -> pluginContext.createIrBuilder(declaration.symbol).irBlockBody {
                +irCall(
                    printlnFunction
                ).apply {
                    arguments[0] = irString("SawClass binding type ${bindingData.bindingType} in ${declaration.callableId.classId} between ${bindingData.navigationKeyName} and ${bindingData.destinationName}")
                }
            }
            GeneratedNavigationBinding.BindingType.Function -> pluginContext.createIrBuilder(declaration.symbol).irBlockBody {
//                +irCall(
//                    printlnFunction
//                ).apply {
//                    arguments[0] = irString("SawFunction binding type ${bindingData.bindingType} in ${declaration.callableId.classId} between ${bindingData.navigationKeyName} and ${bindingData.destinationName}")
//                }
                +irCall(
                    enroSymbols.runtime.navigationModuleBuilderScope.destinationFunction
                ).apply {
                    typeArguments[0] = bindingData.navigationKeyClass.defaultType
                    dispatchReceiver = irGet(scope)

                    arguments.set(
                        enroSymbols.runtime.navigationModuleBuilderScope.destinationFunction.destinationParameter,
                        irCall(enroSymbols.runtime.ui.navigationDestinationProvider.constructor).apply {
                            typeArguments[0] = bindingData.navigationKeyClass.defaultType
                            arguments.set(
                                enroSymbols.runtime.ui.navigationDestinationProvider.constructor.metadataParameter,
                                irBlock {  },
                            )
                            arguments.set(
                                enroSymbols.runtime.ui.navigationDestinationProvider.constructor.contentParameter,
                                irBlock {
                                    +irCall(bindingData.destinationFunction!!)
                                },
                            )
                        }
                    )
                }
            }
            GeneratedNavigationBinding.BindingType.Property -> pluginContext.createIrBuilder(declaration.symbol).irBlockBody {
                +irCall(
                    printlnFunction
                ).apply {
                    arguments[0] = irString("SawProperty binding type ${bindingData.bindingType} in ${declaration.callableId.classId} between ${bindingData.navigationKeyName} and ${bindingData.destinationName}")
                }
            }
        }
        return declaration.also {
//            logger.warn(it.dumpKotlinLike())
        }
    }

    class GeneratedNavigationBindingData(
        private val pluginContext: IrPluginContext,
        private val bindingClass: IrClass,
    ) {
        val navigationKeyName = bindingClass.getAnnotationArgumentValue<String>(
            EnroNames.Annotations.generatedNavigationBinding.asSingleFqName(),
            GeneratedNavigationBinding::navigationKey.name
        ) ?: error("No navigationKeyName name found in $bindingClass")
        val destinationName = bindingClass.getAnnotationArgumentValue<String>(
            EnroNames.Annotations.generatedNavigationBinding.asSingleFqName(),
            GeneratedNavigationBinding::destination.name
        ) ?: error("No destination name found in $bindingClass")
        val bindingType = GeneratedNavigationBinding.BindingType.fromInt(
            value = bindingClass.getAnnotationArgumentValue<Int>(
                EnroNames.Annotations.generatedNavigationBinding.asSingleFqName(),
                GeneratedNavigationBinding::bindingType.name
            ) ?: -1
        )

        val navigationKeyClass by lazy {
            pluginContext.referenceClass(ClassId.fromString(navigationKeyName))
                ?: error("Could not find navigation key class $navigationKeyName")
        }

        val destinationClass by lazy {
            if (bindingType !is  GeneratedNavigationBinding.BindingType.Class) return@lazy null
            pluginContext.referenceClass(ClassId.fromString(destinationName))
                ?: error("Could not find destination class $destinationName")
        }

        val destinationFunction by lazy {
            if (bindingType !is  GeneratedNavigationBinding.BindingType.Function) return@lazy null
            val callableId = CallableId.fromString(destinationName)
            pluginContext.referenceFunctions(CallableId.fromString(destinationName)).singleOrNull()
                ?: error("Could not find destination function $destinationName $ ${callableId.packageName} $ ${callableId.classId} $ ${callableId.callableName}")
        }

        val destinationProperty by lazy {
            if (bindingType !is  GeneratedNavigationBinding.BindingType.Property) return@lazy null
            pluginContext.referenceProperties(CallableId.fromString(destinationName)).singleOrNull()
                ?: error("Could not find destination property $destinationName")
        }
    }
}

fun CallableId.Companion.fromString(
    name: String,
): CallableId {
    val packageName = name.substringBeforeLast('/').replace('/', '.')
    val identifiers = name.substringAfterLast('/').split('.')

    val className = when {
        identifiers.size > 1 -> FqName(identifiers.dropLast(1).joinToString("."))
        else -> null
    }
    val referenceName = identifiers.last()

    return CallableId(
        packageName = FqName(packageName),
        className = className,
        callableName = Name.identifier(referenceName),
    )
}