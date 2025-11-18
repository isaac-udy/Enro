import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
    id("configure-publishing")
}

dependencies {
    implementation(libs.kotlin.stdLib)

    implementation(libs.processing.ksp)

    implementation(libs.processing.incremental)
    kapt(libs.processing.incrementalProcessor)

    compileOnly(libs.processing.autoService)
    kapt(libs.processing.autoService)

    implementation("dev.enro:enro-annotations:${project.enroVersionName}")
    implementation(libs.processing.javaPoet)
    implementation(libs.processing.kotlinPoet)
    implementation(libs.processing.kotlinPoet.ksp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
