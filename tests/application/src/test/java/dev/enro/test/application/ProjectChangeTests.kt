package dev.enro.test.application

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ProjectChangeTests {

    @Before
    fun before() {
        exec("git", "stash", "push", "--include-untracked", ignoreExitValue = true)
    }

    @After
    fun after() {
        exec("git", "add", "-A", ignoreExitValue = true)
        exec("git", "reset", "--hard", ignoreExitValue = true)
        exec("git", "stash", "pop", ignoreExitValue = true)
    }

    @Test
    fun givenFileInApp_whenDestinationFileContentsUpdated_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination.kt")
        val editedContent = editableFile.readText()
            .replace("TestApplicationEditableDestination", "TestApplicationEditableDestination_Edited_1")
            .replace("TestApplicationEditableScreen", "TestApplicationEditableScreen_Edited_1")
        editableFile.writeText(editedContent)
        val renamedFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination_Edited_1.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }

    @Test
    fun givenFileInApp_whenDestinationFileRenamed_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination.kt")
        val renamedFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination_Edited_2.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }

    @Test
    fun givenFileInApp_whenDestinationFileDeleted_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination.kt")
        val renamedFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination_Edited_3.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }

    @Test
    fun givenFileInApp_whenFileIsRenamedAndContentsUpdated_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination.kt")
        val editedContent = editableFile.readText()
            .replace("TestApplicationEditableDestination", "TestApplicationEditableDestination_Edited_4")
            .replace("TestApplicationEditableScreen", "TestApplicationEditableScreen_Edited_4")
        editableFile.writeText(editedContent)
        val renamedFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination_Edited_4.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }


    @Test
    fun givenFileInModule_whenDestinationFileContentsUpdated_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination.kt")
        val editedContent = editableFile.readText()
            .replace("TestModuleEditableDestination", "TestModuleEditableDestination_Edited_5")
            .replace("TestModuleEditableScreen", "TestModuleEditableScreen_Edited_5")
        editableFile.writeText(editedContent)
        val renamedFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination_Edited_5.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }

    @Test
    fun givenFileInModule_whenDestinationFileRenamed_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination.kt")
        val renamedFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination_Edited_6.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }

    @Test
    fun givenFileInModule_whenDestinationFileDeleted_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination.kt")
        val renamedFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination_Edited_7.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }

    @Test
    fun givenFileInModule_whenFileIsRenamedAndContentsUpdated_thenBuildSucceeds() {
        execAssembleDebug()
        val editableFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination.kt")
        val editedContent = editableFile.readText()
            .replace("TestModuleEditableDestination", "TestModuleEditableDestination_Edited_8")
            .replace("TestModuleEditableScreen", "TestModuleEditableScreen_Edited_8")
        editableFile.writeText(editedContent)
        val renamedFile = File("../module-one/src/main/java/dev/enro/tests/module/TestModuleEditableDestination_Edited_8.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }
}

private fun execAssembleDebug() {
    exec("./gradlew", ":tests:application:assembleDebug")
}

private fun exec(
    vararg command: String,
    ignoreExitValue: Boolean = false,
) {
    val javaHome = System.getProperty("java.home")

    ProcessBuilder()
        .command(*command)
        .directory(
            File(".")
                .absoluteFile
                .parentFile!!
                .parentFile!!
                .parentFile!!
        )
        .apply {
            environment().apply {
                put("JAVA_HOME", javaHome)
            }
        }
        .redirectErrorStream(true)
        .start()
        .apply {
            inputStream.use {
                it.bufferedReader()
                    .forEachLine {
                        println("\t$it")
                    }
            }
        }
        .waitFor()
        .let {
            if (it == 0) return@let
            if (ignoreExitValue) return@let
            error("Process exited with code $it")
        }
}