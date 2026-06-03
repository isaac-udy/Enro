plugins {
    id("com.android.application")
}

// Thin runnable APK around :tests:application:common. The Application/Activity classes
// referenced by the manifest, plus all resources, are provided by :common (a library).
// The instrumented test suite lives in :common's androidDeviceTest (same module as the
// content it exercises), so there are no tests here.
android {
    // Distinct from :common's namespace (Android requires unique namespaces per module).
    // The manifest's android:name entries are fully-qualified, so they still resolve to
    // the dev.enro.tests.application.* classes provided by :common. applicationId keeps
    // the original package id.
    namespace = "dev.enro.tests.application.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "dev.enro.tests.application"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":tests:application:common"))
}
