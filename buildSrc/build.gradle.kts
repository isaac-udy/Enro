repositories {
    mavenLocal()
    google()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.compose.gradle)
    implementation(libs.emulator.wtf.gradle)
    implementation(libs.processing.javaPoet) // https://github.com/google/dagger/issues/3068
}

