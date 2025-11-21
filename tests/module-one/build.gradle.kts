import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("configure-compose")
    id("dev.enro.gradle")
}
configureAndroidLibrary("dev.enro.tests.moduleone")

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

dependencies {
    implementation("dev.enro:enro:${project.enroVersionName}")
    implementation("dev.enro:enro-compat:${project.enroVersionName}")
    if (project.hasProperty("enroExampleUseKapt")) {
        kapt("dev.enro:enro-processor:${project.enroVersionName}")
    }
    else {
        ksp("dev.enro:enro-processor:${project.enroVersionName}")
    }

    lintChecks(project(":enro-lint"))

    implementation(libs.compose.material)
    implementation(libs.compose.accompanist.systemUiController)

    implementation(libs.kotlin.stdLib)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)

    implementation(libs.material)
}