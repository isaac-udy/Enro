package dev.enro.compiler

import com.google.auto.service.AutoService
import dev.enro.compiler.fir.EnroFirExtensionRegistrar
import dev.enro.compiler.ir.EnroIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@AutoService(CompilerPluginRegistrar::class)
class EnroComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (configuration[KEY_ENABLED] == false) return

        val logger = EnroLogger(
            messageCollector = configuration.get(
                CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                MessageCollector.NONE,
            )
        )
        FirExtensionRegistrarAdapter.registerExtension(
            EnroFirExtensionRegistrar(
                logger = logger,
            )
        )
        IrGenerationExtension.registerExtension(
            EnroIrGenerationExtension(
                logger = logger,
            )
        )
    }
}