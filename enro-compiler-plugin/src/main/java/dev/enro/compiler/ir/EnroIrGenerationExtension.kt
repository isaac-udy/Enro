package dev.enro.compiler.ir

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.EnroNames
import dev.enro.compiler.ir.transformers.InstallNavigationControllerTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.getAnnotationArgumentValue
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class EnroIrGenerationExtension(
    private val logger: EnroLogger
) : IrGenerationExtension {

    @OptIn(InternalSymbolFinderAPI::class, UnsafeDuringIrConstructionAPI::class)
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val enroSymbols = EnroSymbols(
            moduleFragment = moduleFragment,
            pluginContext = pluginContext,
        )
        val installNavigationControllerTransformer = InstallNavigationControllerTransformer(
            pluginContext = pluginContext,
            symbols = enroSymbols,
            logger = logger,
        )
        val bindFunctionTransformer = BindFunctionTransformer(pluginContext)
        moduleFragment.transform(bindFunctionTransformer, null)
        moduleFragment.transform(installNavigationControllerTransformer, null)
    }

    class BindFunctionTransformer(
        private val pluginContext: IrPluginContext,
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
            val parent = declaration.parent
            if (parent is IrClass && parent.classId != null && declaration !is IrConstructor) {
                val callableId = declaration.callableId
                if (callableId.packageName != EnroNames.Generated.generatedPackage) return declaration
                if (callableId.callableName.isSpecial) return declaration
                if (callableId.callableName.identifier != "bind") return declaration
                val nkName = parent.getAnnotationArgumentValue<String>(
                    EnroNames.Annotations.generatedNavigationBinding.asSingleFqName(),
                    "navigationKey"
                )
                val destinationName = parent.getAnnotationArgumentValue<String>(
                    EnroNames.Annotations.generatedNavigationBinding.asSingleFqName(),
                    "destination"
                )
                val bindingType = parent.getAnnotationArgumentValue<Int>(
                    EnroNames.Annotations.generatedNavigationBinding.asSingleFqName(),
                    "bindingType"
                )
                return declaration.apply {
                    declaration.body = pluginContext.createIrBuilder(declaration.symbol).irBlockBody {
                        +irCall(
                            printlnFunction
                        ).apply {
                            arguments[0] = irString("Saw binding type $bindingType in ${declaration.callableId.classId} between $nkName and $destinationName")
                        }
                    }
                }
            }

            if (declaration.parent !is IrPackageFragment) return declaration
            if (declaration.callableId != EnroNames.Generated.bindFunction) return declaration
            return declaration.apply {
                declaration.body = pluginContext.createIrBuilder(declaration.symbol).irBlockBody {}
            }
        }
    }
}

internal fun IrGeneratorContext.createIrBuilder(symbol: IrSymbol): DeclarationIrBuilder {
    return DeclarationIrBuilder(this, symbol, symbol.owner.startOffset, symbol.owner.endOffset)
}
