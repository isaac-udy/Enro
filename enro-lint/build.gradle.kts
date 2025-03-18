import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("kotlin")
}

dependencies {
    compileOnly(libs.kotlin.stdLib)
    compileOnly(libs.lint.checks)
    compileOnly(libs.lint.api)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    manifest {
        attributes("Lint-Registry-v2" to "dev.enro.lint.EnroIssueRegistry")
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
