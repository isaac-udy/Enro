import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.ComposePlugin

val org.gradle.api.artifacts.dsl.DependencyHandler.compose: ComposePlugin.Dependencies
    get() =
        (this as ExtensionAware).extensions.getByName("compose") as ComposePlugin.Dependencies

val org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension.`compose`: org.jetbrains.compose.ComposePlugin.Dependencies
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("compose") as org.jetbrains.compose.ComposePlugin.Dependencies
