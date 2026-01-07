plugins {
    id("com.google.devtools.ksp")
    id("configure-library")
    id("configure-publishing")
    id("configure-compose")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        desktopMain.dependencies {
            api(libs.compose.ui.backhandler)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
        }
        commonMain.dependencies {
            api("dev.enro:enro-annotations:${project.enroVersionName}")
            api("dev.enro:enro-common:${project.enroVersionName}")
            implementation(libs.compose.runtime)
            implementation(libs.compose.viewmodel)
            implementation(libs.compose.lifecycle)
            implementation(libs.compose.navigationEvent)
            implementation(libs.androidx.savedState)
            implementation(libs.androidx.savedState.compose)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlin.reflect)
            implementation(libs.thauvin.urlencoder)
        }
        commonTest.dependencies {
            implementation(project(":enro-test"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.fragment.compose)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.recyclerview)
            implementation(libs.androidx.lifecycle.process)
            implementation(libs.kotlin.reflect)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlin.js)
        }
    }
}