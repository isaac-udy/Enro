include(":enro-processor")
include(":enro-annotations")
include(":enro-test")
include(":enro-lint")
include(":enro")
include(":enro-common")
include(":enro-runtime")
include(":enro-compat")
include(":recipes:common")
include(":recipes:app:android")
include(":recipes:app:desktop")
include(":recipes:app:ios")
include(":recipes:app:web")

// TEMPORARILY DISABLED during AGP 9.2 migration — migrated in the next milestone
// (KMP+application split into :tests:application:common / :tests:application:app:*).
// include(":tests:application")
// include(":tests:module-one")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}
