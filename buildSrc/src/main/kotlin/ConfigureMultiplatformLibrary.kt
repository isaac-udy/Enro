import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

class ConfigureMultiplatformLibrary : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMultiplatformLibrary()
    }
}

internal fun Project.configureMultiplatformLibrary() {
    val libs = project.the<LibrariesForLibs>()
    project.plugins.apply("com.android.library")
    project.configureKotlinMultiplatform()

    val androidExtension = project.extensions.getByType(LibraryExtension::class.java)
    androidExtension.apply {
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
        testOptions {
            targetSdk = libs.versions.android.targetSdk.get().toInt()
        }
    }
}