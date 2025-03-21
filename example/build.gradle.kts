import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("configure-compose")
}
configureAndroidApp("dev.enro.example")

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}

android {
    buildFeatures {
        buildConfig = false
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

dependencies {
    implementation("dev.enro:enro:${project.enroVersionName}")
    if (project.hasProperty("enroExampleUseKapt")) {
        kapt("dev.enro:enro-processor:${project.enroVersionName}")
    }
    else {
        ksp("dev.enro:enro-processor:${project.enroVersionName}")
    }

    lintChecks(project(":enro-lint"))

    implementation(libs.compose.material)
    implementation(libs.compose.accompanist.systemUiController)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.androidCompiler)

    debugImplementation(libs.leakcanary)

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