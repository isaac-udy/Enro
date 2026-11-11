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
            isMultiplatform -> project.configureComposeMultiplatform(includeUi = true)
            else -> project.configureComposeAndroid()
        }
    }
}

/**
 * Applies the Compose compiler with only the Compose runtime, for multiplatform modules that
 * must not depend on Compose UI.
 */
class ConfigureComposeRuntime : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureComposeMultiplatform(includeUi = false)
    }
}

/**
 * Applies Compose with foundation and UI only, for published modules. Material, tooling and
 * resources are left out so they do not appear in the published dependencies.
 */
class ConfigureComposeUi : Plugin<Project> {
    override fun apply(project: Project) {
        project.configureComposeMultiplatform(includeUi = true, includeSampleLibraries = false)
    }
}

internal fun Project.configureComposeAndroid() {
    plugins.apply("org.jetbrains.compose")
    plugins.apply("org.jetbrains.kotlin.plugin.compose")
    val libs = the<LibrariesForLibs>()
    extensions.configure<BaseExtension> {
        buildFeatures.compose = true
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

internal fun Project.configureComposeMultiplatform(
    includeUi: Boolean,
    includeSampleLibraries: Boolean = includeUi,
) {
    plugins.apply("org.jetbrains.compose")
    plugins.apply("org.jetbrains.kotlin.plugin.compose")

    val libs = the<LibrariesForLibs>()
    val kotlinMultiplatformExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)

    kotlinMultiplatformExtension.apply {
        sourceSets {
            commonMain.dependencies {
                implementation(compose.runtime)
                implementation(libs.compose.viewmodel)
                implementation(libs.compose.bundle)
            }
            if (!includeUi) return@sourceSets

            val desktopMain by getting
            val desktopTest by getting

            androidMain.dependencies {
                implementation(libs.compose.activity)
            }
            commonMain.dependencies {
                implementation(compose.foundation)
                implementation(compose.ui)
            }
            if (!includeSampleLibraries) {
                // Published libraries must not pin consumers to the build machine's OS, so
                // the OS-specific desktop artifact is only on the test classpath.
                desktopMain.dependencies {
                    implementation(compose.desktop.common)
                }
                desktopTest.dependencies {
                    implementation(compose.desktop.currentOs)
                }
                return@sourceSets
            }

            androidMain.dependencies {
                implementation(compose.preview)
                // The KMP library plugin has no build variants, so ui-tooling is added to
                // androidMain rather than a debug-only configuration.
                implementation(compose.uiTooling)
            }
            commonMain.dependencies {
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
            desktopMain.dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
    // No `buildFeatures { compose = true }` needed here — the Compose compiler is applied
    // across all KMP targets (incl. Android) by the `org.jetbrains.kotlin.plugin.compose`
    // plugin above.
}