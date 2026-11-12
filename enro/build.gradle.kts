import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.google.devtools.ksp")
    id("configure-library-with-js")
    id("configure-publishing")
    id("configure-compose-ui")
    kotlin("plugin.serialization")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
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
            api(project(":enro-ui"))
            api("dev.enro:enro-annotations:${project.enroVersionName}")
        }

        androidMain.dependencies {

        }
    }
}

// The lintPublish configuration is declared in the top-level dependencies block. A
// declaration inside the kotlin source set dependencies has not been made to work.
dependencies {
    lintPublish(project(":enro-lint"))
}
