import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.google.devtools.ksp")
    id("configure-library")
    id("kotlin-kapt")
    id("wtf.emulator.gradle")
    id("configure-publishing")
    id("configure-compose")
    kotlin("plugin.serialization")
}
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
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xfriend-paths=../enro-core/src/main")
    }
}

kotlin {
    sourceSets {
        desktopMain.dependencies {

        }
        commonMain.dependencies {
            api("dev.enro:enro-core:${project.enroVersionName}")
            api("dev.enro:enro-annotations:${project.enroVersionName}")
        }

        androidMain.dependencies {

        }
        androidUnitTest.dependencies {
            implementation(libs.testing.junit)
            implementation(libs.testing.androidx.junit)
            implementation(libs.testing.androidx.runner)
            implementation(libs.testing.robolectric)
            implementation("dev.enro:enro-test:${project.enroVersionName}")
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

            implementation(libs.testing.androidx.fragment)
            implementation(libs.testing.androidx.junit)
            implementation(libs.testing.androidx.espresso)
            implementation(libs.testing.androidx.espressoRecyclerView)
            implementation(libs.testing.androidx.espressoIntents)
            implementation(libs.testing.androidx.runner)

            implementation(libs.testing.androidx.compose)
            implementation(libs.compose.materialIcons)

            implementation(libs.androidx.navigation.fragment)
            implementation(libs.androidx.navigation.ui)

            implementation(libs.leakcanary)
            implementation(libs.testing.leakcanary.instrumentation)
        }
    }
}

// Some android dependencies need to be declared at the top level like this,
// it's a bit gross but I can't figure out how to get it to work otherwise
dependencies {
    lintPublish(project(":enro-lint"))
    kaptAndroidTest("dev.enro:enro-processor:${project.enroVersionName}")
}
