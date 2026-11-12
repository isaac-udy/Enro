// Snippets for docs/ghpages/docs/migrating-from-v2.md
package dev.enro.docs.migrating

import androidx.compose.ui.test.junit4.createComposeRule
import dev.enro.test.EnroTestRule
import org.junit.Rule
import org.junit.Test

// region docs:migrating/enro-test-rule
class ProfileTest {
    @get:Rule(order = 0) val enroRule = EnroTestRule()
    @get:Rule(order = 1) val composeRule = createComposeRule()

    @Test fun example() { /* ... */ }
}
// endregion
