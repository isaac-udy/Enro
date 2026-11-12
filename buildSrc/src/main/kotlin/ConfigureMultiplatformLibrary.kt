import org.gradle.api.Plugin
import org.gradle.api.Project

class ConfigureMultiplatformLibrary : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMultiplatformLibrary(js = false)
    }
}

class ConfigureMultiplatformLibraryWithJs : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMultiplatformLibrary(js = true)
    }
}

/** A JS library that requires a browser DOM: the library has no other target and no Node.js tests. */
class ConfigureBrowserLibrary : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureKotlinMultiplatform(
            android = false,
            ios = false,
            wasmJs = false,
            desktop = false,
            js = true,
            jsNodejs = false,
        )
    }
}

internal fun Project.configureMultiplatformLibrary(
    js: Boolean,
) {
    // The KMP plugin must be applied before the Android-KMP library plugin, which adds the
    // `androidLibrary {}` DSL to the KMP extension. configureKotlinMultiplatform sets
    // minSdk, compileSdk and namespace through that DSL.
    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    project.plugins.apply("com.android.kotlin.multiplatform.library")
    project.configureKotlinMultiplatform(js = js)
}