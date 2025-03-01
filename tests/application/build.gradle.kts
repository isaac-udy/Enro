import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("com.google.devtools.ksp")
    id("wtf.emulator.gradle")
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("configure-compose")
}
configureAndroidApp("dev.enro.test.application")
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

dependencies {
    implementation(project(":tests:module-one"))

    implementation(project(":enro"))
    if (project.hasProperty("enroExampleUseKapt")) {
        kapt(project(":enro-processor"))
    }
    else {
        ksp(project(":enro-processor"))
    }

    lintChecks(project(":enro-lint"))

//     Uncomment the following line to enable leakcanary
//    debugImplementation(libs.leakcanary)

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

    testImplementation(libs.testing.junit)

    androidTestImplementation(project(":enro-test"))

    androidTestImplementation(libs.testing.junit)

    androidTestImplementation(libs.kotlin.reflect)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.fragment)
    androidTestImplementation(libs.androidx.activity)
    androidTestImplementation(libs.androidx.recyclerview)

    androidTestImplementation(libs.testing.androidx.espresso)

    androidTestImplementation(libs.testing.androidx.fragment)
    androidTestImplementation(libs.testing.androidx.junit)
    androidTestImplementation(libs.testing.androidx.espresso)
    androidTestImplementation(libs.testing.androidx.espressoRecyclerView)
    androidTestImplementation(libs.testing.androidx.espressoIntents)
    androidTestImplementation(libs.testing.androidx.runner)

    androidTestImplementation(libs.testing.androidx.compose)
}
