package dev.enro.gradle

import dev.enro.gradle.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class EnroGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("enro", EnroGradlePluginExtension::class.java)
    }

    override fun getCompilerPluginId(): String = "dev.enro.plugin.enro-compiler"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "dev.enro",
        artifactId = "enro-compiler-plugin",
        version = BuildConfig.VERSION,
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(EnroGradlePluginExtension::class.java)
        val enabled = extension.enabled.get()

        return project.provider {
            listOf(
                SubpluginOption(key = "enabled", value = enabled.toString()),
            )
        }
    }
}