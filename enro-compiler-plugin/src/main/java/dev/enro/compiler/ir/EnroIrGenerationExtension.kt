package dev.enro.compiler.ir

import dev.enro.compiler.EnroLogger
import dev.enro.compiler.ir.transformers.InstallNavigationControllerTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

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
            enroSymbols = enroSymbols,
            logger = logger,
        )
        moduleFragment.transform(installNavigationControllerTransformer, null)
    }
}

internal fun IrGeneratorContext.createIrBuilder(symbol: IrSymbol): DeclarationIrBuilder {
    return DeclarationIrBuilder(this, symbol, symbol.owner.startOffset, symbol.owner.endOffset)
}
