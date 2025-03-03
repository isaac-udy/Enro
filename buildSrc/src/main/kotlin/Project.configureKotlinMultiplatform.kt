import com.android.build.api.dsl.*
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

internal fun Project.configureKotlinMultiplatform(
    android: Boolean = true,
    ios: Boolean = true,
    frontendJs: Boolean = true,
    desktop: Boolean = true,
) {

    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    if (android) {
        project.plugins.apply("org.jetbrains.kotlin.plugin.parcelize")
    }

    val libs = project.the<LibrariesForLibs>()

    val kotlinMultiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
    kotlinMultiplatformExtension.apply {
        explicitApi = ExplicitApiMode.Strict

        if (android) {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    freeCompilerArgs.addAll(
                        "-P",
                        "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=dev.enro.annotations.Parcelize"
                    )
                    optIn.addAll(
                        "dev.enro.annotations.AdvancedEnroApi",
                        "dev.enro.annotations.ExperimentalEnroApi",
                    )
                }
            }
        }

        if (desktop) {
            jvm("desktop") {
                @OptIn(ExperimentalKotlinGradlePluginApi::class)
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    optIn.addAll(
                        "dev.enro.annotations.AdvancedEnroApi",
                        "dev.enro.annotations.ExperimentalEnroApi",
                    )
                }
            }
        }

        if (frontendJs) {
            wasmJs("frontendJs") {
                moduleName = project.projectName.camelCase
                browser {
                    commonWebpackConfig {
                        outputFileName = "${project.projectName.camelCase}.js"
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static = (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(project.projectDir.path)
                            }
                        }
                    }
                }
                binaries.executable()
                compilerOptions {
                    optIn.addAll(
                        "dev.enro.annotations.AdvancedEnroApi",
                        "dev.enro.annotations.ExperimentalEnroApi",
                    )
                }
            }
        }

        if (ios) {
            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = project.projectName.pascalCase
                    isStatic = true
                    compilerOptions {
                        optIn.addAll(
                            "dev.enro.annotations.AdvancedEnroApi",
                            "dev.enro.annotations.ExperimentalEnroApi",
                        )
                    }
                }
            }
        }

        sourceSets {
            commonMain.dependencies {
                implementation(kotlin("stdlib-common"))
            }
            if (android) {
                androidMain.dependencies {
                    implementation(kotlin("stdlib"))
                }
            }

            if (desktop) {
                val desktopMain by getting
                desktopMain.dependencies {
                }
            }
        }
    }

    if (android) {
        @Suppress("UNCHECKED_CAST")
        val androidExtension =
            project.extensions.getByType(CommonExtension::class) as CommonExtension<BuildFeatures, BuildType, DefaultConfig, ProductFlavor, AndroidResources, Installation>

        androidExtension.apply {
            namespace = when(project.projectName.packageName) {
                "enro.core" -> "dev.enro.core"
                else -> project.projectName.packageName
            }
            compileSdk = libs.versions.android.compileSdk.get().toInt()

            sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
            sourceSets["main"].res.srcDirs("src/androidMain/res")
            sourceSets["main"].resources.srcDirs("src/commonMain/resources")

            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}