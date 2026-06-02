import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.google.devtools.ksp")
    id("configure-library")
    id("configure-publishing")
    id("configure-compose")
    kotlin("plugin.serialization")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-Xfriend-paths=../enro-core/src/main")
    }
}

kotlin {
    // AGP 9: per-module Android config that used to live in the top-level `android {}`
    // block now goes on the KMP library target. (testOptions.animationsDisabled was
    // dropped — this module has no Android instrumented tests.)
    android {
        lint {
            textReport = true
        }
        packaging {
            resources.excludes.add("META-INF/*")
        }
    }
    sourceSets {
        desktopMain.dependencies {

        }
        commonMain.dependencies {
            api("dev.enro:enro-common:${project.enroVersionName}")
            api("dev.enro:enro-runtime:${project.enroVersionName}")
            api("dev.enro:enro-annotations:${project.enroVersionName}")
        }

        androidMain.dependencies {

        }
        // NOTE: the previous androidUnitTest / androidInstrumentedTest dependency
        // blocks were removed during the AGP 9 migration — this module has no Android
        // test sources (they live in :tests:application), so those source sets and
        // their dependencies were dead config.
    }
}

// Some android dependencies need to be declared at the top level like this,
// it's a bit gross but I can't figure out how to get it to work otherwise
dependencies {
    lintPublish(project(":enro-lint"))
}
