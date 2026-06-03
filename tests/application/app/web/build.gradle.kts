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

// Keep the generated Compose resources package stable as
// `enro.tests.application.generated.resources` (matching the entry point's imports),
// rather than letting it derive from the :app:web module path.
compose.resources {
    packageOfResClass = "enro.tests.application.generated.resources"
    generateResClass = always
}
