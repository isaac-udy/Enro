import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
    id("configure-publishing")
}

dependencies {
    implementation("dev.enro:enro-annotations:${project.enroVersionName}")
    compileOnly(libs.kotlin.stdLib)
    compileOnly(libs.kotlin.compiler.embeddable)

    compileOnly(libs.processing.autoService)
    kapt(libs.processing.autoService)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        // Apply options globally
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}
