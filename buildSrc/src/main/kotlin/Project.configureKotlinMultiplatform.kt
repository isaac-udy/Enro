
import com.android.build.api.dsl.*
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

private val optIns = arrayOf(
    "dev.enro.annotations.AdvancedEnroApi",
    "dev.enro.annotations.ExperimentalEnroApi",
    "kotlin.uuid.ExperimentalUuidApi",
    "kotlin.io.encoding.ExperimentalEncodingApi",
    "kotlin.experimental.ExperimentalObjCName",
    "kotlinx.serialization.ExperimentalSerializationApi",
)

internal fun Project.configureKotlinMultiplatform(
    android: Boolean = true,
    ios: Boolean = true,
    wasmJs: Boolean = true,
    js: Boolean = true,
    desktop: Boolean = true,
) {

    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    if (android) {
        project.plugins.apply("org.jetbrains.kotlin.plugin.parcelize")
    }

    val libs = project.the<LibrariesForLibs>()

    val kotlinMultiplatformExtension =
        project.extensions.getByType(KotlinMultiplatformExtension::class.java)
    kotlinMultiplatformExtension.apply {
        explicitApi = ExplicitApiMode.Strict

        if (desktop) {
            jvm("desktop") {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                    freeCompilerArgs.addAll("-Xexpect-actual-classes")
                    optIn.addAll(*optIns)
                }
            }
        }

        if (wasmJs) {
            wasmJs {
                outputModuleName.set(project.projectName.camelCase)
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
                    compilerOptions {
                        sourceMap.set(true)
                        sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
                    }
                }
                binaries.executable()
                compilerOptions {
                    freeCompilerArgs.addAll("-Xexpect-actual-classes", "-Xwasm-attach-js-exception")
                    freeCompilerArgs.add("-Xwasm-kclass-fqn")
                    optIn.addAll(*optIns)
                }
            }
        }

        if (js) {
            js {
                nodejs()
                binaries.executable()
                compilations["main"].packageJson {
                    main = "$projectName-backend.js"
                    version = "1.0.0"
                    customField("engines", mapOf("node" to "22"))
                    private = true
                }
                compilerOptions {
                    sourceMap.set(true)
                    sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
                }
                compilerOptions.sourceMap.set(true)
                compilerOptions.sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
            }
        }


        if (ios) {
            listOf(
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = project.projectName.pascalCase
                    isStatic = true
                    compilerOptions {
                        freeCompilerArgs.addAll("-Xexpect-actual-classes")
                        optIn.addAll(*optIns)
                    }
                }
            }
        }

        sourceSets {
            commonMain.dependencies {
                implementation(kotlin("stdlib-common"))
            }
            commonTest.dependencies {
                implementation(kotlin("test"))
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
        // Configure the Android library target contributed by the
        // `com.android.kotlin.multiplatform.library` plugin. In build scripts this is the
        // `kotlin { androidLibrary { ... } }` accessor; from a binary convention plugin we
        // reach the same target via the KMP extension's ExtensionAware container.
        val androidNamespace = project.projectName.packageName
        val compileSdkVersion = libs.versions.android.compileSdk.get().toInt()
        val minSdkVersion = libs.versions.android.minSdk.get().toInt()
        // "androidLibrary" (rather than the "android" alias) so this resolves on AGP 9.0,
        // whose Android plugin is the latest currently supported by IntelliJ. Both names
        // refer to the same target on AGP 9.1+.
        val androidLibrary = (kotlinMultiplatformExtension as ExtensionAware).extensions
            .getByName("androidLibrary") as KotlinMultiplatformAndroidLibraryTarget
        androidLibrary.apply {
            namespace = androidNamespace
            compileSdk = compileSdkVersion
            minSdk = minSdkVersion

            // The KMP library plugin disables Android resource processing (and the
            // generated R class) by default; enable it so modules with src/androidMain/res
            // (and code that references R) build. No-op for modules without resources.
            androidResources {
                enable = true
            }

            // Android test components (`withHostTest`/`withDeviceTest`) can each be
            // enabled at most once, so they're opted into per-module rather than here
            // (only enro-runtime currently runs Android host tests for its commonTest).

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
                freeCompilerArgs.addAll(
                    "-P",
                    "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=dev.enro.annotations.Parcelize"
                )
                freeCompilerArgs.addAll("-Xexpect-actual-classes")
                optIn.addAll(*optIns)
            }
        }
    }
}