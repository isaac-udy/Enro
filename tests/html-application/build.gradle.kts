plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        outputModuleName.set("enroHtmlApplication")
        browser {
            commonWebpackConfig {
                outputFileName = "enroHtmlApplication.js"
            }
        }
        binaries.executable()
        compilerOptions {
            optIn.addAll(
                "dev.enro.annotations.ExperimentalEnroApi",
                "kotlin.uuid.ExperimentalUuidApi",
            )
        }
    }
    sourceSets {
        jsMain.dependencies {
            implementation(project(":enro-html"))
            implementation(compose.runtime)
            implementation(libs.compose.viewmodel)
            implementation(libs.kotlinx.serialization)
        }
    }
}

dependencies {
    add("kspJs", "dev.enro:enro-processor:${project.enroVersionName}")
}
