import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    sourceSets {
        commonMain.dependencies {
            // `api` so the split-out per-platform app modules (:recipes:app:*) get the
            // Enro APIs (incl. platform entry points like EnroUIViewController /
            // GenericRootWindow / EnroBrowserContent) transitively from :recipes:common.
            api("dev.enro:enro:${project.enroVersionName}")
            api("dev.enro:enro-compat:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.lifecycle)
        }

        desktopMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.savedState)
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
