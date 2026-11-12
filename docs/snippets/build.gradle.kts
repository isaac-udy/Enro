import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// Holds the code samples shown on the docs site (docs/ghpages) as real Kotlin, so they are
// compiled on every target. `checkDocsSnippets` compares the Markdown with the regions in
// this module; see ConfigureDocsSnippets in buildSrc.
plugins {
    id("configure-library")
    id("com.google.devtools.ksp")
    id("configure-compose")
    kotlin("plugin.serialization")
    id("configure-docs-snippets")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled

    sourceSets {
        commonMain.dependencies {
            implementation("dev.enro:enro:${project.enroVersionName}")
            implementation("dev.enro:enro-compat:${project.enroVersionName}")
            // Test samples live in main source sets: they are compiled, never run.
            implementation("dev.enro:enro-test:${project.enroVersionName}")
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
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
