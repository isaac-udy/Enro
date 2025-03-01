import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("configure-compose")
}
configureAndroidLibrary("dev.enro.tests.moduleone")

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

dependencies {
    implementation(project(":enro"))
    if (project.hasProperty("enroExampleUseKapt")) {
        kapt(project(":enro-processor"))
    }
    else {
        ksp(project(":enro-processor"))
    }

    lintChecks(project(":enro-lint"))

    implementation(libs.compose.material)
    implementation(libs.compose.accompanist.systemUiController)

    implementation(libs.kotlin.stdLib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)

    implementation(libs.material)
}