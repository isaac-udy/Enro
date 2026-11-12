import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// The Metro recipes are in a separate module so that the Metro compiler
// plugin is applied to the Metro recipes only.
plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
    kotlin("plugin.serialization")
    id("dev.zacsweers.metro")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    sourceSets {
        commonMain.dependencies {
            implementation("dev.enro:enro:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.lifecycle)
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
