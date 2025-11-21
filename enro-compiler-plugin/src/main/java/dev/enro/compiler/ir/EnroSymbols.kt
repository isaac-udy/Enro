package dev.enro.compiler.ir

import dev.enro.compiler.EnroNames
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId

class EnroSymbols(
    private val moduleFragment: IrModuleFragment,
    val pluginContext: IrPluginContext,
) {

    val installNavigationController: IrSimpleFunctionSymbol by lazy {
        pluginContext
            .referenceFunctions(
                EnroNames.Runtime.installNavigationController
            )
            .first()
    }

    val bindFunctions: Collection<IrSimpleFunctionSymbol> by lazy {
        pluginContext.referenceFunctions(
            EnroNames.Generated.bindFunction
        )
    }

    val internalCreateEnroController: IrSimpleFunctionSymbol by lazy {
        pluginContext
            .referenceFunctions(
                EnroNames.Runtime.internalCreateEnroController
            )
            .first()
    }
}