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
    // AGP 9: KMP libraries use the dedicated Android-KMP library plugin instead of
    // `com.android.library` (which can no longer be applied in the same module as the
    // Kotlin Multiplatform plugin). The KMP plugin is applied first so the
    // `androidLibrary {}` DSL it contributes is available; minSdk/compileSdk/namespace
    // are configured there (see configureKotlinMultiplatform).
    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    project.plugins.apply("com.android.kotlin.multiplatform.library")
    project.configureKotlinMultiplatform(js = js)
}