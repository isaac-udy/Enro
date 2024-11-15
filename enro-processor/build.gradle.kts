import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}
configureJavaPublishing("dev.enro:enro-processor")

dependencies {
    implementation(libs.kotlin.stdLib)

    implementation(libs.processing.ksp)

    implementation(libs.processing.incremental)
    kapt(libs.processing.incrementalProcessor)

    implementation(libs.processing.autoService)
    kapt(libs.processing.autoService)

    implementation(project(":enro-annotations"))
    implementation(libs.processing.javaPoet)
    implementation(libs.processing.kotlinPoet)
    implementation(libs.processing.kotlinPoet.ksp)
}

afterEvaluate {
    tasks.findByName("compileKotlin")
            ?.dependsOn(":enro-annotations:publishToMavenLocal")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}