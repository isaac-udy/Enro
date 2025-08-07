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
import java.util.Properties

fun Project.configureAndroidLibrary(
    namespace: String
) {
    commonAndroidConfig(namespace = namespace)
    extensions.configure<LibraryExtension> {
        buildFeatures {
            buildConfig = false
            viewBinding = true
        }
    }
}

fun Project.configureAndroidApp(
    namespace: String
) {
    commonAndroidConfig(namespace = namespace)
    extensions.configure<ApplicationExtension> {
        buildFeatures {
            buildConfig = false
            viewBinding = true
        }
    }
}

private fun Project.commonAndroidConfig(
    namespace: String
) {
    val versionProperties = Properties()
    versionProperties.load(FileInputStream(rootProject.file("version.properties")))

    extensions.configure<BaseExtension> {
        this@configure.namespace = namespace
        compileSdkVersion(35)
        defaultConfig {
            minSdk = 21
            targetSdk = 34
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

            // We want to disable the automatic inclusion of the `dev.enro.annotations.AdvancedEnroApi` and `dev.enro.annotations.ExperimentalEnroApi`
            // opt-ins when we're compiling the test application, so that we're not accidentally making changes that might break the public API by
            // requiring the opt-ins.
            if (path.startsWith(":tests:application")) {
                return@compilerOptions
            }
            freeCompilerArgs.add("-Xopt-in=dev.enro.annotations.AdvancedEnroApi")
            freeCompilerArgs.add("-Xopt-in=dev.enro.annotations.ExperimentalEnroApi")
        }
    }

    val libs = the<LibrariesForLibs>()
    dependencies {
        add("implementation", libs.kotlin.stdLib)
    }
}
