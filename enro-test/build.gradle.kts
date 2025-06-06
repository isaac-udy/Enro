import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("configure-library")
    id("configure-publishing")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xfriend-paths=../enro-core/src/main")
    }
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    sourceSets {
        desktopMain.dependencies {
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.lifecycle)
            implementation(libs.androidx.viewmodel)
            api("dev.enro:enro-core:${project.enroVersionName}")
        }
        androidMain.dependencies {

            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)

            implementation(libs.testing.junit)
            implementation(libs.testing.androidx.runner)
            implementation(libs.testing.androidx.core)
            implementation(libs.testing.androidx.espresso)
            //noinspection FragmentGradleConfiguration
            implementation(libs.testing.androidx.fragment)

        }
    }
}

