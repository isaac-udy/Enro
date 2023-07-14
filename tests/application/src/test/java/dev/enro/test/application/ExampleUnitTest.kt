package dev.enro.test.application

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Before
    fun before() {
        exec("git", "stash", "push", "--include-untracked")
    }

    @After
    fun after() {
        exec("git", "add", "-A")
        exec("git", "reset", "--hard")
        exec("git", "stash", "pop")
    }

    @Test
    fun renamingFileAndContentsBuildsSuccessfully() {
        execAssembleDebug()
        val editableFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination.kt")
        val editedContent = """
            package dev.enro.tests.application
            
            import androidx.compose.material.Text
            import androidx.compose.runtime.Composable
            import dev.enro.annotations.NavigationDestination
            import dev.enro.core.NavigationKey
            import kotlinx.parcelize.Parcelize
            
            @Parcelize
            internal class TestApplicationEditableDestination_Edited : NavigationKey.SupportsPush
            
            @Composable
            @NavigationDestination(TestApplicationEditableDestination_Edited::class)
            internal fun TestApplicationEditableScreen_Edited() {
                Text("Test Screen (Edited)")
            }
        """.trimIndent()

        editableFile.writeText(editedContent)
        val renamedFile = File("src/main/java/dev/enro/tests/application/TestApplicationEditableDestination_Edited.kt")
        editableFile.renameTo(renamedFile)
        execAssembleDebug()
    }
}

private fun execAssembleDebug() {
    exec("./gradlew", ":tests:application:assembleDebug")
}

private fun exec(
    vararg command: String
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
            error("Process exited with code $it")
        }
}