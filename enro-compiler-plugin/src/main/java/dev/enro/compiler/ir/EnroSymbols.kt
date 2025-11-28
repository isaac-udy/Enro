package dev.enro.compiler.ir

import dev.enro.compiler.EnroNames
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol

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

    val bindingReferenceFunctions: Collection<IrSimpleFunctionSymbol> by lazy {
        pluginContext.referenceFunctions(
            EnroNames.Generated.bindingReferenceFunction
        )
    }

    val internalCreateEnroController: IrSimpleFunctionSymbol by lazy {
        pluginContext
            .referenceFunctions(
                EnroNames.Runtime.internalCreateEnroController
            )
            .first()
    }

    val runtime = Runtime()

    inner class Runtime {
        val navigationModuleBuilderScope by lazy {
            NavigationModuleBuilderScope(
                pluginContext.referenceClass(EnroNames.Runtime.navigationModuleBuilderScope)!!
            )
        }

        inner class NavigationModuleBuilderScope(
            private val symbol: IrClassSymbol,
        ) : IrClassSymbol by symbol {
            val destinationFunction by lazy {
                DestinationFunction(
                    pluginContext
                        .referenceFunctions(EnroNames.Runtime.NavigationModuleBuilderScope.destinationFunction)
                        .first()
                )
            }

            inner class DestinationFunction(
                private val symbol: IrSimpleFunctionSymbol,
            ) : IrSimpleFunctionSymbol by symbol {
                val destinationParameter by lazy {
                    symbol.owner.parameters.single { it.name.identifierOrNullIfSpecial == "destination" }
                }
            }
        }

        val ui = Ui()

        inner class Ui {
            val navigationDestinationProvider by lazy {
                NavigationDestinationProvider(
                    pluginContext.referenceClass(EnroNames.Runtime.Ui.navigationDestinationProvider)!!
                )
            }

            inner class NavigationDestinationProvider(
                private val symbol: IrClassSymbol,
            ) : IrClassSymbol by symbol {

                val constructor by lazy {
                    Constructor(
                        pluginContext.referenceConstructors(EnroNames.Runtime.Ui.navigationDestinationProvider)
                            .single()
                    )
                }

                inner class Constructor(
                    private val symbol: IrConstructorSymbol,
                ) : IrConstructorSymbol by symbol {
                    val metadataParameter by lazy {
                        symbol.owner.parameters.single { it.name.identifierOrNullIfSpecial == "metadata" }
                    }
                    val contentParameter by lazy {
                        symbol.owner.parameters.single { it.name.identifierOrNullIfSpecial == "content" }
                    }
                }
            }
        }
    }
}