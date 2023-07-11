plugins {
    id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    id("com.android.library")
    id("kotlin-kapt")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("wtf.emulator.gradle")
}
configureAndroidLibrary("dev.enro")
configureCompose()
configureAndroidPublishing("dev.enro:enro")
configureEmulatorWtf()

android {
    lint {
        textReport = true
    }
    testOptions {
        animationsDisabled = true
    }
    packaging {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    releaseApi("dev.enro:enro-core:${android.defaultConfig.versionName}")
    debugApi(project(":enro-core"))

    releaseApi("dev.enro:enro-annotations:${android.defaultConfig.versionName}")
    debugApi(project(":enro-annotations"))

    lintPublish(project(":enro-lint"))

    kaptAndroidTest(project(":enro-processor"))

    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.androidx.junit)
    testImplementation(libs.testing.androidx.runner)
    testImplementation(libs.testing.robolectric)
    testImplementation(project(":enro-test"))

    androidTestImplementation(project(":enro-test"))

    androidTestImplementation(libs.testing.junit)

    androidTestImplementation(libs.kotlin.reflect)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.fragment)
    androidTestImplementation(libs.androidx.activity)
    androidTestImplementation(libs.androidx.recyclerview)

    androidTestImplementation(libs.testing.androidx.fragment)
    androidTestImplementation(libs.testing.androidx.junit)
    androidTestImplementation(libs.testing.androidx.espresso)
    androidTestImplementation(libs.testing.androidx.espressoRecyclerView)
    androidTestImplementation(libs.testing.androidx.espressoIntents)
    androidTestImplementation(libs.testing.androidx.runner)

    androidTestImplementation(libs.testing.androidx.compose)

    androidTestImplementation(libs.androidx.navigation.fragment)
    androidTestImplementation(libs.androidx.navigation.ui)

    androidTestImplementation(libs.leakcanary)
    androidTestImplementation(libs.testing.leakcanary.instrumentation)
}

afterEvaluate {
    tasks.named("preReleaseBuild") {
        dependsOn(
            ":enro-core:publishToMavenLocal",
            ":enro-annotations:publishToMavenLocal"
        )
    }
}
