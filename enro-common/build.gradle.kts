plugins {
    id("com.google.devtools.ksp")
    id("configure-library-with-js")
    id("configure-publishing")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api("dev.enro:enro-annotations:${project.enroVersionName}")
            implementation(libs.androidx.savedState)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlin.reflect)
            implementation(libs.thauvin.urlencoder)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
        }
    }
}
