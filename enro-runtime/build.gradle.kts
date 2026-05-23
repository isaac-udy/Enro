plugins {
    id("com.google.devtools.ksp")
    id("configure-library")
    id("configure-publishing")
    id("configure-compose")
    kotlin("plugin.serialization")
}

android {
    testOptions {
        unitTests {
            // Return default values for unmocked Android framework methods
            // rather than throwing "Method ... not mocked" — defensive against
            // any test path that brushes a stub from android.jar (e.g.
            // savedstate's Bundle).
            isReturnDefaultValues = true
        }
        unitTests.all {
            // These tests use Compose-UI-test or platform SavedState APIs
            // that require a real Android runtime (Robolectric or instrumentation)
            // to function. They already run on :enro-runtime:desktopTest
            // (and iosSimulatorArm64Test) — keep coverage there and skip the
            // JVM-only Android unit-test pass.
            it.filter {
                excludeTestsMatching("dev.enro.SceneHarnessSmokeTest")
                excludeTestsMatching("dev.enro.SceneIntegrationTests")
                excludeTestsMatching("dev.enro.BackstackSavedStateTests")
            }
        }
    }
}

kotlin {
    sourceSets {
        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
        }
        commonMain.dependencies {
            api("dev.enro:enro-annotations:${project.enroVersionName}")
            api("dev.enro:enro-common:${project.enroVersionName}")
            implementation(libs.compose.runtime)
            api(libs.compose.viewmodel)
            implementation(libs.compose.lifecycle)
            api(libs.compose.navigationEvent)
            implementation(libs.androidx.savedState)
            implementation(libs.androidx.savedState.compose)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlin.reflect)
            implementation(libs.thauvin.urlencoder)
        }
        commonTest.dependencies {
            implementation(project(":enro-test"))
            implementation(libs.compose.uiTest)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.fragment.compose)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.recyclerview)
            implementation(libs.androidx.lifecycle.process)
            implementation(libs.kotlin.reflect)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlin.js)
        }
    }
}