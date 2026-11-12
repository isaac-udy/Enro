plugins {
    id("com.android.application")
}

// A runnable APK for :tests:application:common. :common (a library) provides the
// Application and Activity classes named in the manifest, and all resources. The
// instrumented tests are in :common's androidDeviceTest source set, so this module
// has no tests.
android {
    // Android requires a unique namespace per module, so the namespace differs from
    // :common's namespace. The manifest's android:name entries are fully qualified
    // and resolve to the dev.enro.tests.application.* classes in :common.
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
