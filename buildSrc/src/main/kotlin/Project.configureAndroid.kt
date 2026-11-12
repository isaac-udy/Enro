
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

fun Project.configureAndroidLibrary(
    namespace: String
) {
    commonAndroidConfig(namespace = namespace)
    extensions.configure<LibraryExtension> {
        buildFeatures {
            buildConfig = true
            viewBinding = false
        }
    }
}

fun Project.configureAndroidApp(
    namespace: String
) {
    commonAndroidConfig(namespace = namespace)
    extensions.configure<ApplicationExtension> {
        buildFeatures {
            buildConfig = true
            viewBinding = false
        }
    }
}

private fun Project.commonAndroidConfig(
    namespace: String
) {
    val versionProperties = Properties()
    versionProperties.load(FileInputStream(rootProject.file("version.properties")))

    extensions.configure<BaseExtension> {
        val libs = project.the<LibrariesForLibs>()
        this@configure.namespace = namespace
        compileSdkVersion(libs.versions.android.compileSdk.get().toInt())

        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
            targetSdk = libs.versions.android.targetSdk.get().toInt()
            versionCode = versionProperties.getProperty("versionCode").toInt()
            versionName = versionProperties.getProperty("versionName")

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            consumerProguardFiles("consumer-rules.pro")
        }

        buildTypes {
            getByName("release") {
                minifyEnabled(false)
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    tasks.withType<KotlinCompile>() {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)

            // The test application compiles without the AdvancedEnroApi and ExperimentalEnroApi
            // opt-ins, so that a public API change that requires an opt-in fails to compile in
            // the test application.
            if (path.startsWith(":tests:application")) {
                return@compilerOptions
            }
            freeCompilerArgs.add("-Xopt-in=dev.enro.annotations.AdvancedEnroApi")
            freeCompilerArgs.add("-Xopt-in=dev.enro.annotations.ExperimentalEnroApi")
            freeCompilerArgs.add("-Xopt-in=kotlin.uuid.ExperimentalUuidApi")
        }
    }

    val libs = the<LibrariesForLibs>()
    dependencies {
        add("implementation", libs.kotlin.stdLib)
    }
}
