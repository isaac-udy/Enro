import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
    id("wtf.emulator.gradle")
    kotlin("plugin.serialization")
}

configureEmulatorWtf()

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    // AGP 9: the test-app content is now a KMP *library*. Its Android host (unit) and
    // device (instrumented) test components are enabled here so the 53 instrumented tests
    // keep same-module visibility to the content/destinations they exercise. The runnable
    // APK / desktop / web / iOS entry points live in the sibling :app:* modules.
    androidLibrary {
        // Override the convention's path-derived namespace (which would become
        // dev.enro.tests.application.common) so the generated R class stays at
        // dev.enro.tests.application.R, matching the existing source imports.
        namespace = "dev.enro.tests.application"

        withHostTest {}
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        desktopMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.savedState)
        }
        commonMain.dependencies {
            // `api` so the split-out :app:* modules get the Enro APIs (incl. platform
            // entry points) and the test-app content transitively from :common.
            api("dev.enro:enro:${project.enroVersionName}")
            api("dev.enro:enro-compat:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.lifecycle)
        }

        androidMain.dependencies {
            implementation(project(":tests:module-one"))

            implementation(libs.compose.material)
            implementation(libs.compose.accompanist.systemUiController)

            implementation(libs.kotlin.reflect)

            implementation(libs.kotlin.stdLib)
            implementation(libs.androidx.core)
            implementation(libs.androidx.splashscreen)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.viewmodel)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.fragment.compose)
            implementation(libs.androidx.activity)

            implementation(libs.material)
        }
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/core", "5.6.5"))
        }
    }
}

dependencies {
    val enroProcessor = "dev.enro:enro-processor:${project.enroVersionName}"
    add("kspCommonMainMetadata", enroProcessor)
    add("kspAndroid", enroProcessor)
    add("kspDesktop", enroProcessor)
    add("kspWasmJs", enroProcessor)
    add("kspIosArm64", enroProcessor)
    add("kspIosSimulatorArm64", enroProcessor)

    lintChecks(project(":enro-lint"))

    // Android host (unit) + device (instrumented) test dependencies. Declared via
    // configuration names because the new KMP library plugin creates the
    // androidHostTest / androidDeviceTest source sets dynamically (no DSL accessor).
    "androidHostTestImplementation"(libs.testing.junit)

    "androidDeviceTestImplementation"("dev.enro:enro-test:${project.enroVersionName}")
    "androidDeviceTestImplementation"(libs.testing.junit)
    "androidDeviceTestImplementation"(libs.kotlin.reflect)
    "androidDeviceTestImplementation"(libs.androidx.core)
    "androidDeviceTestImplementation"(libs.androidx.appcompat)
    "androidDeviceTestImplementation"(libs.androidx.fragment)
    "androidDeviceTestImplementation"(libs.androidx.activity)
    "androidDeviceTestImplementation"(libs.androidx.recyclerview)
    "androidDeviceTestImplementation"(libs.testing.androidx.espresso)
    "androidDeviceTestImplementation"(libs.testing.androidx.fragment)
    "androidDeviceTestImplementation"(libs.testing.androidx.junit)
    "androidDeviceTestImplementation"(libs.testing.androidx.espressoRecyclerView)
    "androidDeviceTestImplementation"(libs.testing.androidx.espressoIntents)
    "androidDeviceTestImplementation"(libs.testing.androidx.runner)
    "androidDeviceTestImplementation"(libs.testing.androidx.compose)
}
