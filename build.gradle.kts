import java.io.FileInputStream
import java.util.Properties

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.kotlin.serialization.gradle)
        classpath(libs.processing.ksp.gradle)
        classpath(libs.emulator.wtf.gradle)
        classpath(libs.maven.publish.gradle)
        classpath(libs.dokka.gradle)
        // Used only by :recipes:metro.
        classpath(libs.metro.gradle)
    }
}

// The root `dokkaGenerate` task combines the Dokka output of each `dokka(...)` dependency
// below into one HTML site at `build/dokka/html`. `ConfigurePublishing` applies Dokka to a
// module, so a module that does not apply `configure-publishing` is not in the API reference.
apply(plugin = "org.jetbrains.dokka")

dependencies {
    "dokka"(project(":enro"))
    "dokka"(project(":enro-annotations"))
    "dokka"(project(":enro-common"))
    "dokka"(project(":enro-compat"))
    "dokka"(project(":enro-processor"))
    "dokka"(project(":enro-runtime"))
    "dokka"(project(":enro-test"))
    "dokka"(project(":enro-ui"))
    "dokka"(project(":enro-html"))
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("dev.enro:enro-common"))
                .using(project(":enro-common"))

            substitute(module("dev.enro:enro-runtime"))
                .using(project(":enro-runtime"))

            substitute(module("dev.enro:enro-ui"))
                .using(project(":enro-ui"))

            substitute(module("dev.enro:enro-html"))
                .using(project(":enro-html"))

            substitute(module("dev.enro:enro-test"))
                .using(project(":enro-test"))

            substitute(module("dev.enro:enro-compat"))
                .using(project(":enro-compat"))

            substitute(module("dev.enro:enro-annotations"))
                .using(project(":enro-annotations"))

            substitute(module("dev.enro:enro-processor"))
                .using(project(":enro-processor"))

            substitute(module("dev.enro:enro"))
                .using(project(":enro"))
        }
    }
}

subprojects {
    afterEvaluate {
        tasks.register("continuousIntegration") {
            val continuousIntegration = this
            tasks.findByName("lintDebug")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("testAndroidHostTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("desktopTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("wasmJsBrowserTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("jsTest")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("test")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("testWithEmulatorWtf")?.let { continuousIntegration.dependsOn(it) }
            // Fails when the public API differs from the dump committed under `api/`.
            // Run `./gradlew updateKotlinAbi` to record an intended change.
            tasks.findByName("checkKotlinAbi")?.let { continuousIntegration.dependsOn(it) }
            // Fails when a docs code sample differs from the compiled snippet.
            // Run `./gradlew updateDocsSnippets` to rewrite the docs from the snippets.
            tasks.findByName("checkDocsSnippets")?.let { continuousIntegration.dependsOn(it) }
            // The compile tasks make CI compile the modules without tests (for example, the
            // recipes). For a module with tests, the test tasks above already depend on
            // the compile tasks.
            tasks.findByName("compileKotlinDesktop")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("compileAndroidMain")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("compileKotlinWasmJs")?.let { continuousIntegration.dependsOn(it) }
            tasks.findByName("compileKotlinJs")?.let { continuousIntegration.dependsOn(it) }
        }

        // The macOS CI runner runs only the iOS tests, to limit macOS runner minutes.
        // continuousIntegration runs every other test on Linux.
        tasks.register("continuousIntegrationMacOs") {
            val ci = this
            tasks.findByName("iosSimulatorArm64Test")?.let { ci.dependsOn(it) }
            // The compile task makes a Kotlin/Native compile failure fail CI for a module
            // without iOS tests.
            tasks.findByName("compileKotlinIosSimulatorArm64")?.let { ci.dependsOn(it) }
            // The device target is published and has no tests. Without this compile task,
            // the release job is the first job that compiles the device target.
            tasks.findByName("compileKotlinIosArm64")?.let { ci.dependsOn(it) }
        }
    }
}

tasks.register("updateVersion") {
    doLast {
        if (!project.hasProperty("versionName")) {
            error("The updateVersion task requires a versionName property to be passed as an argument")
        }
        val versionPropertiesFile = rootProject.file("version.properties")
        val existingProperties = Properties()
        existingProperties.load(FileInputStream(versionPropertiesFile))

        val versionName = project.properties["versionName"]
        val versionCode = (existingProperties["versionCode"].toString().toInt()) + 1

        if(versionName == existingProperties["versionName"]) {
            error("The versionName '$versionName' is the current versionName")
        }

        // CHANGELOG.md lists changes under a standing "## Unreleased" header. A release adds
        // a header with the version and date below "## Unreleased", and writes the released
        // section to build/release-notes.md, which the release workflow attaches to the
        // GitHub release. Every validation runs before the first file write, so a failed
        // validation changes no file.
        val changelogFile = rootProject.file("CHANGELOG.md")
        val changelog = changelogFile.readText()
        val unreleasedHeader = "## Unreleased"
        val headerIndex = changelog.indexOf(unreleasedHeader)
        if (headerIndex == -1) {
            error("CHANGELOG.md must contain an '## Unreleased' section to release from")
        }
        val bodyStart = headerIndex + unreleasedHeader.length
        val nextHeaderIndex = changelog.indexOf("\n## ", bodyStart)
            .let { if (it == -1) changelog.length else it }
        val releaseNotesBody = changelog.substring(bodyStart, nextHeaderIndex).trim()
        if (releaseNotesBody.isEmpty()) {
            error(
                "The 'Unreleased' section of CHANGELOG.md is empty — " +
                    "add release notes before releasing $versionName"
            )
        }

        val releaseNotesFile = rootProject.layout.buildDirectory.file("release-notes.md").get().asFile
        releaseNotesFile.parentFile.mkdirs()
        releaseNotesFile.writeText(releaseNotesBody + "\n")

        val releaseDate = java.time.LocalDate.now()
        changelogFile.writeText(
            changelog.replaceFirst(
                unreleasedHeader,
                "## Unreleased\n\n## $versionName ($releaseDate)",
            )
        )

        versionPropertiesFile.writeText("versionName=$versionName\nversionCode=$versionCode")
    }
}

tasks.register<Exec>("publishEnroLocal") {
    group = "publishing"
    description = "Publishes Enro libraries to Maven Local"

    workingDir = rootProject.projectDir
    commandLine(
        "./gradlew",
        ":enro-processor:publishMavenPublicationToMavenLocal",
        ":enro-annotations:publishAndroidPublicationToMavenLocal",
        ":enro-annotations:publishDesktopPublicationToMavenLocal",

        "publishKotlinMultiplatformPublicationToMavenLocal",
        "publishAndroidPublicationToMavenLocal",
        "publishDesktopPublicationToMavenLocal",
        "publishIosArm64PublicationToMavenLocal",
        "publishIosSimulatorArm64PublicationToMavenLocal",

        "--no-parallel", "-Dorg.gradle.workers.max=1"
    )
}
