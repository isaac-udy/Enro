plugins {
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("configure-library")
    id("configure-publishing")
    id("configure-compose")
}

kotlin {
    sourceSets {
        desktopMain.dependencies {
        }
        commonMain.dependencies {
            api("dev.enro:enro-annotations:${project.enroVersionName}")
            implementation(libs.compose.viewmodel)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlin.reflect)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.recyclerview)
            implementation(libs.androidx.lifecycle.process)

            compileOnly(libs.hilt.android)
            compileOnly(libs.androidx.navigation.fragment)

        }
    }
}

dependencies {
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.androidCompiler)
}