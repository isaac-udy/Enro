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

include(":tests:module-one")
include(":tests:application:common")
include(":tests:application:app:android")
include(":tests:application:app:desktop")
include(":tests:application:app:ios")
include(":tests:application:app:web")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}
