import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("enroTestApplication")
        browser {
            commonWebpackConfig {
                outputFileName = "enroTestApplication.js"
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
            implementation(project(":tests:application:common"))
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(npm("@js-joda/core", "5.6.5"))
        }
    }
}

// The entry point imports `enro.tests.application.generated.resources`. Without
// `packageOfResClass`, the Compose resources package is derived from the :app:web
// module path.
compose.resources {
    packageOfResClass = "enro.tests.application.generated.resources"
    generateResClass = always
}
