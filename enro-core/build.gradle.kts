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
            implementation(libs.kotlin.reflect)
        }
        commonMain.dependencies {
            api(project(":enro-annotations"))
            implementation(libs.compose.viewmodel)
            implementation(libs.benasher.uuid)
            implementation(libs.kotlinx.serialization)
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