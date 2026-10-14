plugins {
    id("configure-browser-library")
    id("configure-publishing")
    id("configure-compose-runtime")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        jsMain.dependencies {
            api("dev.enro:enro-runtime:${project.enroVersionName}")
            api(libs.compose.html.core)
            implementation(libs.compose.lifecycle)
            implementation(libs.androidx.savedState.compose)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.serialization)
        }
        jsTest.dependencies {
            implementation(project(":enro-test"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
