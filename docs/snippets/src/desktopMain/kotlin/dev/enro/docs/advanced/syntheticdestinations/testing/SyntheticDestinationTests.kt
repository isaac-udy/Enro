// Snippets for docs/ghpages/docs/advanced/synthetic-destinations.md
//
// The test samples are in desktopMain because test names with spaces do not
// compile on every target.
package dev.enro.docs.advanced.syntheticdestinations.testing

import dev.enro.NavigationKey
import dev.enro.controller.createNavigationModule
import dev.enro.test.assertOpens
import dev.enro.test.assertSideEffect
import dev.enro.test.installNavigationModule
import dev.enro.test.runEnroTest
import dev.enro.test.runWith
import dev.enro.test.testSyntheticDestination
import dev.enro.ui.destinations.syntheticDestination
import kotlinx.serialization.Serializable
import kotlin.test.Test

@Serializable
data object RequireProtectedFeature : NavigationKey

@Serializable
data object AuthGateProtectedFeature : NavigationKey

@Serializable
data object AuthGateLogin : NavigationKey

@Serializable
data class OpenExternalUrl(val url: String) : NavigationKey

private var isLoggedIn = false

private object sessionRepository {
    var isLoggedIn = false
    fun signOut() {
        isLoggedIn = false
    }
}

val requireProtectedFeature = syntheticDestination<RequireProtectedFeature> {
    if (isLoggedIn) open(AuthGateProtectedFeature) else open(AuthGateLogin)
}

val openExternalUrl = syntheticDestination<OpenExternalUrl> {
    sideEffect { }
}

// region docs:synthetic-destinations/test-registered
@Test
fun `auth gate forwards to protected screen when logged in`() = runEnroTest {
    installNavigationModule(
        createNavigationModule {
            destination<RequireProtectedFeature>(requireProtectedFeature)
        }
    )
    isLoggedIn = true  // app-state setup

    val outcome = testSyntheticDestination(RequireProtectedFeature)

    outcome.assertOpens<AuthGateProtectedFeature>()
}
// endregion

// region docs:synthetic-destinations/test-direct
val authGate = syntheticDestination<RequireProtectedFeature> {
    if (sessionRepository.isLoggedIn) open(AuthGateProtectedFeature)
    else open(AuthGateLogin)
}

@Test
fun `auth gate redirects to login when logged out`() {
    sessionRepository.signOut()

    val outcome = testSyntheticDestination(RequireProtectedFeature, authGate)

    outcome.assertOpens<AuthGateLogin>()
}
// endregion

// region docs:synthetic-destinations/test-side-effect
@Test
fun `external URL synthetic runs the launch side effect`() {
    val outcome = testSyntheticDestination(OpenExternalUrl("https://enro.dev"), openExternalUrl)
    outcome.assertSideEffect().runWith()
    // …assert on whatever the side effect produced (mock state, captured intents, etc.)
}
// endregion
