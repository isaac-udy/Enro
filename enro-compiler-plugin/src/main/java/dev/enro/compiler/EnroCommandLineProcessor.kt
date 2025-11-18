package dev.enro.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class)
class EnroCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "enroPlugin"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = "enabled",
            valueDescription = "<true|false>",
            description = "whether to enable the plugin or not",
            required = false,
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) = when (option.optionName) {
        "enabled" -> configuration.put(KEY_ENABLED, value.toBoolean())
        else -> configuration.put(KEY_ENABLED, true)
    }
}

val KEY_ENABLED = CompilerConfigurationKey<Boolean>("whether the plugin is enabled")