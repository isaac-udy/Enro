
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

private val consumerOptIns = arrayOf(
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
    jsNodejs: Boolean = true,
    desktop: Boolean = true,
) {

    project.plugins.apply("org.jetbrains.kotlin.multiplatform")
    if (android) {
        project.plugins.apply("org.jetbrains.kotlin.plugin.parcelize")
    }

    val libs = project.the<LibrariesForLibs>()

    // The Enro libraries are the root-level modules. The test and recipe apps nested under the
    // root-level modules compile without the InternalEnroApi opt-in, as consumer code does, so
    // KSP output that requires InternalEnroApi fails to compile in those apps.
    val isEnroLibrary = project.parent == project.rootProject
    val optIns = when {
        isEnroLibrary -> consumerOptIns + "dev.enro.annotations.InternalEnroApi"
        else -> consumerOptIns
    }

    val kotlinMultiplatformExtension =
        project.extensions.getByType(KotlinMultiplatformExtension::class.java)
    kotlinMultiplatformExtension.apply {
        explicitApi = ExplicitApiMode.Strict

        if (isEnroLibrary) {
            // The committed dumps under each module's `api/` directory are the semver contract.
            // Advanced, Experimental and Internal APIs are outside the contract, so the dumps
            // exclude those APIs.
            @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
            abiValidation {
                filters {
                    exclude {
                        annotatedWith.add("dev.enro.annotations.AdvancedEnroApi")
                        annotatedWith.add("dev.enro.annotations.ExperimentalEnroApi")
                        annotatedWith.add("dev.enro.annotations.InternalEnroApi")
                    }
                }
            }
        }

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
            // The webpack block is stored in the configuration cache, and a Project read inside
            // the block stores the Project in the cache. The block reads these locals instead.
            val webpackOutputFileName = "${project.projectName.camelCase}.js"
            val projectDirPath = project.projectDir.path
            wasmJs {
                outputModuleName.set(project.projectName.camelCase)
                browser {
                    commonWebpackConfig {
                        outputFileName = webpackOutputFileName
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static = (static ?: mutableListOf()).apply {
                                // The dev server serves the sources for debugging in the browser.
                                add(projectDirPath)
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
                    freeCompilerArgs.addAll("-Xexpect-actual-classes")
                    freeCompilerArgs.add("-Xwasm-kclass-fqn")
                    optIn.addAll(*optIns)
                }
            }
        }

        if (js) {
            js {
                browser()
                if (jsNodejs) nodejs()
                binaries.executable()
                compilerOptions {
                    sourceMap.set(true)
                    sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
                    freeCompilerArgs.addAll("-Xexpect-actual-classes")
                    optIn.addAll(*optIns)
                }
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
        // The `com.android.kotlin.multiplatform.library` plugin adds the Android library target.
        // A build script uses the `kotlin { androidLibrary { ... } }` accessor; a binary
        // convention plugin reads the same target from the extensions of the KMP extension.
        val androidNamespace = project.projectName.packageName
        val compileSdkVersion = libs.versions.android.compileSdk.get().toInt()
        val minSdkVersion = libs.versions.android.minSdk.get().toInt()
        // The name is "androidLibrary", not the "android" alias, so that the lookup resolves on
        // AGP 9.0, the latest AGP that IntelliJ supports. Both names refer to the same target
        // on AGP 9.1+.
        val androidLibrary = (kotlinMultiplatformExtension as ExtensionAware).extensions
            .getByName("androidLibrary") as KotlinMultiplatformAndroidLibraryTarget
        androidLibrary.apply {
            namespace = androidNamespace
            compileSdk = compileSdkVersion
            minSdk = minSdkVersion

            // The KMP library plugin disables Android resource processing and the generated R
            // class by default. Modules with src/androidMain/res require both.
            androidResources {
                enable = true
            }

            // `withHostTest` and `withDeviceTest` can each be enabled at most once, so each
            // module enables the Android test components in the module's build script.

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