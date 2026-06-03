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
    androidLibrary {
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
    }
}

// Some android dependencies need to be declared at the top level like this,
// it's a bit gross but I can't figure out how to get it to work otherwise
dependencies {
    lintPublish(project(":enro-lint"))
}
