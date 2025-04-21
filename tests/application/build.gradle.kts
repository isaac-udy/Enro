import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("configure-application")
    id("com.google.devtools.ksp")
    id("wtf.emulator.gradle")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}

configureEmulatorWtf()

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    buildTypes {
        getByName("debug") {
            // Set isTestingProguard to true to test proguard rules
            // this is useful when testing bugs that are related to minification
            val isTestingProguard = false

            if (isTestingProguard) {
                isDebuggable = false
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
}


kotlin {
    sourceSets {
        desktopMain.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.savedState)
        }
        commonMain.dependencies {
            implementation("dev.enro:enro:${project.enroVersionName}")
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.lifecycle)
        }

        androidMain.dependencies {
            implementation(project(":tests:module-one"))

            implementation(libs.compose.material)
            implementation(libs.compose.accompanist.systemUiController)

            implementation(libs.kotlin.reflect)

            implementation(libs.kotlin.stdLib)
            implementation(libs.androidx.core)
            implementation(libs.androidx.splashscreen)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.lifecycle)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.activity)

            implementation(libs.material)
        }
        androidUnitTest.dependencies {
            implementation(libs.testing.junit)
        }
        androidInstrumentedTest.dependencies {
            implementation("dev.enro:enro-test:${project.enroVersionName}")
            implementation(libs.testing.junit)
            implementation(libs.kotlin.reflect)
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.recyclerview)
            implementation(libs.testing.androidx.espresso)
            implementation(libs.testing.androidx.fragment)
            implementation(libs.testing.androidx.junit)
            implementation(libs.testing.androidx.espresso)
            implementation(libs.testing.androidx.espressoRecyclerView)
            implementation(libs.testing.androidx.espressoIntents)
            implementation(libs.testing.androidx.runner)
            implementation(libs.testing.androidx.compose)
        }
    }
}

// Some android dependencies need to be declared at the top level like this,
// it's a bit gross but I can't figure out how to get it to work otherwise
dependencies {
//     Uncomment the following line to enable leakcanary
//    debugImplementation(libs.leakcanary)

    if (project.hasProperty("enroExampleUseKapt")) {
        kapt("dev.enro:enro-processor:${project.enroVersionName}")
    }
    else {
        ksp("dev.enro:enro-processor:${project.enroVersionName}")
    }

    lintChecks(project(":enro-lint"))
}
