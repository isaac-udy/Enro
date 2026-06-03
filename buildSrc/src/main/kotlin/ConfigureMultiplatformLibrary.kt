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

internal fun Project.configureMultiplatformLibrary(
    js: Boolean,
) {
    // KMP libraries with an Android target use the dedicated Android-KMP library plugin.
    // The KMP plugin is applied first so the `androidLibrary {}` DSL it contributes is
    // available; minSdk/compileSdk/namespace are configured there (see
    // configureKotlinMultiplatform).
    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    project.plugins.apply("com.android.kotlin.multiplatform.library")
    project.configureKotlinMultiplatform(js = js)
}