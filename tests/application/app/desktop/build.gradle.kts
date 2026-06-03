import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            optIn.addAll(
                "dev.enro.annotations.AdvancedEnroApi",
                "dev.enro.annotations.ExperimentalEnroApi",
                "kotlin.uuid.ExperimentalUuidApi",
            )
        }
    }
    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(project(":tests:application:common"))
            implementation(compose.desktop.currentOs)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.androidx.savedState)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.enro.tests.application"
            packageVersion = "1.0.0"
        }
    }
}
