// Snippets for docs/ghpages/docs/advanced/testing.md
package dev.enro.docs.advanced.testing

import androidx.compose.ui.test.junit4.createComposeRule
import dev.enro.test.EnroTestRule
import org.junit.Rule

class ProfileScreenTest {
    // region docs:testing/enro-test-rule
    @get:Rule(order = 0)
    val enroRule = EnroTestRule()

    @get:Rule(order = 1)
    val composeRule = createComposeRule()
    // endregion
}
