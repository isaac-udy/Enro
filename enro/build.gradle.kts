import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("kotlin-kapt")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("wtf.emulator.gradle")
    id("configure-publishing")
    id("configure-compose")
}
configureAndroidLibrary("dev.enro")
configureEmulatorWtf(numShards = 4)

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xfriend-paths=../enro-core/src/main")
    }
}

dependencies {
    api("dev.enro:enro-core:${project.enroVersionName}")

    api("dev.enro:enro-annotations:${project.enroVersionName}")
    lintPublish(project(":enro-lint"))

    kaptAndroidTest("dev.enro:enro-processor:${project.enroVersionName}")

    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.androidx.junit)
    testImplementation(libs.testing.androidx.runner)
    testImplementation(libs.testing.robolectric)
    testImplementation("dev.enro:enro-test:${project.enroVersionName}")

    androidTestImplementation("dev.enro:enro-test:${project.enroVersionName}")

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
    androidTestImplementation(libs.compose.materialIcons)

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
