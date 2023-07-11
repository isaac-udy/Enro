plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}
configureAndroidLibrary("dev.enro.core")
configureCompose()
configureExplicitApi()
configureAndroidPublishing("dev.enro:enro-core")

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.process)

    compileOnly(libs.androidx.navigation.fragment)
    compileOnly(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.androidCompiler)

    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.archunit)
    testImplementation(libs.kotlin.reflect)
}