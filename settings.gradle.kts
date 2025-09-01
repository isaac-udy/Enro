rootProject.name = "Enro"

include(":enro-processor")
include(":enro-annotations")
include(":enro-test")
include(":enro-lint")
include(":enro")
include(":enro-core")
include(":enro-compat")
include(":tests:application")
include(":tests:module-one")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}
