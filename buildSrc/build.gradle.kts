import org.jetbrains.kotlin.gradle.dsl.JvmTarget

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.compose.compiler.gradle)
    implementation(libs.compose.gradle)
    implementation(libs.emulator.wtf.gradle)
    implementation(libs.maven.publish.gradle)
}


gradlePlugin {
    plugins {
        register("configure-application") {
            id = "configure-application"
            implementationClass = "ConfigureMultiplatformApplication"
        }
        register("configure-library") {
            id = "configure-library"
            implementationClass = "ConfigureMultiplatformLibrary"
        }
        register("configure-publishing") {
            id = "configure-publishing"
            implementationClass = "ConfigurePublishing"
        }
        register("configure-compose") {
            id = "configure-compose"
            implementationClass = "ConfigureCompose"
        }
    }
}