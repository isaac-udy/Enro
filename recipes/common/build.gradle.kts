import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    // The Material 3 Adaptive library used by two recipes needs API 24. Enro
    // itself supports the minSdk in libs.versions.toml.
    androidLibrary {
        minSdk = 24
    }

    sourceSets {
        commonMain.dependencies {
            // `api` so the per-platform app modules (:recipes:app:*) get the Enro APIs
            // (incl. platform entry points like EnroUIViewController / GenericRootWindow /
            // EnroBrowserContent) transitively from :recipes:common.
            api("dev.enro:enro:${project.enroVersionName}")
            api("dev.enro:enro-compat:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.lifecycle)
            // The Modular Navigation recipe's modules. The feature modules are
            // only here to put their generated bindings on the classpath.
            implementation(project(":recipes:modular:navigation"))
            implementation(project(":recipes:modular:feature-home"))
            implementation(project(":recipes:modular:feature-profile"))
            // The Metro recipes, in their own module for the Metro compiler plugin.
            implementation(project(":recipes:metro"))
            // For the Material list-detail and supporting pane recipes.
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.layout)
            // For the Koin recipes.
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        // For the Fragment and Activity recipe.
        androidMain.dependencies {
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.activity)
        }

        desktopMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.savedState)
        }

        // Tests run on desktop only, so CI does not link Compose test binaries
        // for wasm and iOS. The test code itself uses only common APIs.
        desktopTest.dependencies {
            implementation("dev.enro:enro-test:${project.enroVersionName}")
            implementation(libs.compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
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
}
