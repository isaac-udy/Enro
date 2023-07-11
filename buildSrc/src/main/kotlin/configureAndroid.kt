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
import org.jetbrains.kotlin.gradle.model.KotlinAndroidExtension
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
        compileSdkVersion(33)
        defaultConfig {
            minSdk = 21
            targetSdk = 33
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
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    tasks.withType<KotlinCompile>() {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()

            freeCompilerArgs += "-Xjvm-default=enable"
            freeCompilerArgs += "-Xopt-in=dev.enro.core.AdvancedEnroApi"
            freeCompilerArgs += "-Xopt-in=dev.enro.core.ExperimentalEnroApi"
        }
    }

    val libs = the<LibrariesForLibs>()
    dependencies {
        add("implementation", libs.kotlin.stdLib)
    }
}
