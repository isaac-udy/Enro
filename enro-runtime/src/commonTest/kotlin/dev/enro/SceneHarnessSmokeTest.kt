package dev.enro

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

/**
 * Smoke test for the Compose multiplatform test harness.
 *
 * Establishes that `runComposeUiTest` works from `:enro-runtime`'s
 * commonTest on the desktop target. Once this is green, scene-strategy /
 * scene-decorator / overlay tests can layer on top, replacing what would
 * otherwise need to live as instrumented robot tests.
 */
@OptIn(ExperimentalTestApi::class)
class SceneHarnessSmokeTest {

    @Test
    fun `runComposeUiTest renders a simple composable`() = runComposeUiTest {
        setContent {
            Text("hello harness")
        }
        onNodeWithText("hello harness").assertIsDisplayed()
    }
}
