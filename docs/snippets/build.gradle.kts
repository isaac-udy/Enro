import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

// Contains the code samples shown on the docs site (docs/ghpages) as Kotlin source, so every
// target compiles the samples. `checkDocsSnippets` compares the Markdown with the regions in
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
            // Test samples are in main source sets: the test samples are compiled, never run.
            implementation("dev.enro:enro-test:${project.enroVersionName}")
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.lifecycle)
        }

        // kotlin("test") selects a test framework for test source sets only.
        desktopMain.dependencies {
            implementation(kotlin("test-junit"))
        }

        androidMain.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.testing.androidx.compose)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.appcompat)
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
