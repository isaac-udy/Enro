plugins {
    id("java-library")
    id("kotlin")
}
configureJavaPublishing("dev.enro:enro-annotations")
configureExplicitApi()

dependencies {
    implementation(libs.kotlin.stdLib)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}