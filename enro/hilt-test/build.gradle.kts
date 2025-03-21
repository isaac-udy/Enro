plugins {
    id("dagger.hilt.android.plugin")
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("configure-compose")
}
configureAndroidLibrary("dev.enro.hilt.test")

android {
    defaultConfig {
        testInstrumentationRunner = "dev.enro.HiltTestApplicationRunner"
    }
    packaging {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    implementation("dev.enro:enro:${project.enroVersionName}")

    kaptAndroidTest("dev.enro:enro-processor:${project.enroVersionName}")

    androidTestImplementation("dev.enro:enro-test:${project.enroVersionName}")
    androidTestImplementation(libs.testing.junit)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.fragment)
    androidTestImplementation(libs.androidx.activity)
    androidTestImplementation(libs.androidx.recyclerview)
    androidTestImplementation(libs.hilt.android)
    androidTestImplementation(libs.hilt.testing)
    kaptAndroidTest(libs.hilt.compiler)
    kaptAndroidTest(libs.hilt.androidCompiler)

    androidTestImplementation(libs.testing.androidx.fragment)
    androidTestImplementation(libs.testing.androidx.junit)
    androidTestImplementation(libs.testing.androidx.espresso)
    androidTestImplementation(libs.testing.androidx.espressoRecyclerView)
    androidTestImplementation(libs.testing.androidx.espressoIntents)
    androidTestImplementation(libs.testing.androidx.runner)

    androidTestImplementation(libs.testing.androidx.compose)
}