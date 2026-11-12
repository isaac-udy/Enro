package dev.enro

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

// Checks that `runComposeUiTest` runs in the `:enro-runtime` test source sets.
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
