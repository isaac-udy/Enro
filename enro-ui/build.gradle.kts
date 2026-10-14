plugins {
    id("configure-library-with-js")
    id("configure-publishing")
    id("configure-compose")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api("dev.enro:enro-runtime:${project.enroVersionName}")
            implementation(libs.compose.lifecycle)
            implementation(libs.kotlinx.serialization)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
