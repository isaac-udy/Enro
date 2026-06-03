import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("recipes")
        browser {
            commonWebpackConfig {
                outputFileName = "recipes.js"
            }
        }
        binaries.executable()
        compilerOptions {
            optIn.addAll(
                "dev.enro.annotations.AdvancedEnroApi",
                "dev.enro.annotations.ExperimentalEnroApi",
                "kotlin.uuid.ExperimentalUuidApi",
            )
        }
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":recipes:common"))
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.components.resources)
        }
    }
}
