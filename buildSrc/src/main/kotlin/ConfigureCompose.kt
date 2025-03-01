import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.Installation
import com.android.build.api.dsl.ProductFlavor
import com.android.build.gradle.BaseExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ConfigureCompose : Plugin<Project> {
    override fun apply(project: Project) {
        val isMultiplatform = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
        when {
            isMultiplatform -> project.configureComposeMultiplatform()
            else -> project.configureComposeAndroid()
        }
    }
}

internal fun Project.configureComposeAndroid() {
    plugins.apply("org.jetbrains.compose")
    plugins.apply("org.jetbrains.kotlin.plugin.compose")
    val libs = the<LibrariesForLibs>()
    extensions.configure<BaseExtension> {
        buildFeatures.compose = true
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.10"
        }
    }

    dependencies {
        add("implementation", libs.compose.compiler)
        add("implementation", libs.compose.foundation)
        add("implementation", libs.compose.foundationLayout)
        add("implementation", libs.compose.ui)
        add("implementation", libs.compose.uiTooling)
        add("implementation", libs.compose.runtime)
        add("implementation", libs.compose.viewmodel)
        add("implementation", libs.compose.livedata)
        add("implementation", libs.compose.activity)
        add("implementation", libs.compose.material)
    }
}

internal fun Project.configureComposeMultiplatform() {
    plugins.apply("org.jetbrains.compose")
    plugins.apply("org.jetbrains.kotlin.plugin.compose")

    val libs = the<LibrariesForLibs>()
    val kotlinMultiplatformExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)

    kotlinMultiplatformExtension.apply {
        sourceSets {
            val desktopMain by getting

            androidMain.dependencies {
                implementation(compose.preview)
                implementation(libs.compose.activity)
            }
            commonMain.dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(libs.compose.viewmodel)
                implementation(libs.compose.bundle)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
            desktopMain.dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val androidExtension =
        project.extensions.getByType(CommonExtension::class) as CommonExtension<BuildFeatures, BuildType, DefaultConfig, ProductFlavor, AndroidResources, Installation>

    androidExtension.apply {
        buildFeatures {
            compose = true
        }
        project.dependencies {
            "debugImplementation"(compose.uiTooling)
        }
    }
}