import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("configure-publishing")
}
configureAndroidLibrary("dev.enro.test")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xfriend-paths=../enro-core/src/main")
    }
}

dependencies {
    releaseApi("dev.enro:enro-core:${android.defaultConfig.versionName}")
    debugApi(project(":enro-core"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)

    implementation(libs.testing.junit)
    implementation(libs.testing.androidx.runner)
    implementation(libs.testing.androidx.core)
    implementation(libs.testing.androidx.espresso)
    //noinspection FragmentGradleConfiguration
    implementation(libs.testing.androidx.fragment)
}

afterEvaluate {
    tasks.findByName("preReleaseBuild")
            ?.dependsOn(":enro-core:publishToMavenLocal")
}