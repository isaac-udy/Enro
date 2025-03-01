import com.android.build.api.dsl.ApplicationExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.the
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

class ConfigureMultiplatformApplication : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureMultiplatformApplication()
    }
}

internal fun Project.configureMultiplatformApplication() {
    val libs = project.the<LibrariesForLibs>()
    project.plugins.apply("com.android.application")
    project.configureKotlinMultiplatform()

    val compose = project.extensions.getByType(ComposeExtension::class.java)
    compose as ExtensionAware
    compose.extensions.configure<DesktopExtension>("desktop") {
        application {
            mainClass = "MainKt"
            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = project.projectName.packageName
                packageVersion = "1.0.0"
            }
        }
    }

    val androidExtension = project.extensions.getByType(ApplicationExtension::class.java)
    androidExtension.apply {
        defaultConfig {
            applicationId = project.projectName.packageName
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
            multiDexEnabled = true
        }
    }
}