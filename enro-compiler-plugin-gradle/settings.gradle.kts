pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs { maybeCreate("libs").apply { from(files("../libs.versions.toml")) } }
    repositories { mavenCentral() }
}