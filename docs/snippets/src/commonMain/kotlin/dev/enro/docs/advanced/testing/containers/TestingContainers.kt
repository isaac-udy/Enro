// Snippets for docs/ghpages/docs/advanced/testing.md
package dev.enro.docs.advanced.testing.containers

import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.docs.advanced.pathbindings.programmatic.ProductDetail
import dev.enro.docs.advanced.pathbindings.programmatic.productDetailBinding
import dev.enro.NavigationOperation
import dev.enro.test.EnroTest
import dev.enro.test.assertActive
import dev.enro.test.assertBackstackContains
import dev.enro.test.assertBackstackDoesNotContain
import dev.enro.test.assertBackstackKeys
import dev.enro.test.assertBackstackSize
import dev.enro.test.assertPathDoesNotResolve
import dev.enro.test.assertPathFor
import dev.enro.test.assertPathResolvesTo
import dev.enro.test.fixtures.NavigationContainerFixtures
import dev.enro.test.installPathBindings
import dev.enro.test.runEnroTest
import kotlinx.serialization.Serializable
import kotlin.test.Test

@Serializable
data object Home : NavigationKey

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

// region docs:testing/container-fixture
@Test
fun opening_a_profile_adds_it_to_the_backstack() = runEnroTest {
    val container = NavigationContainerFixtures.create(
        backstack = backstackOf(Home.asInstance()),
    )

    container.execute(NavigationOperation.Open(ShowProfile("user-123").asInstance()))

    container.assertBackstackSize(2)
    container.assertBackstackKeys(Home, ShowProfile("user-123"))
    container.assertBackstackContains<ShowProfile> { it.userId == "user-123" }
    container.assertBackstackDoesNotContain<ProductDetail>()
    container.assertActive<ShowProfile>()
}
// endregion

// region docs:testing/path-assertions
@Test
fun product_paths_resolve() = runEnroTest {
    installPathBindings(productDetailBinding)
    val controller = EnroTest.getCurrentNavigationController()

    controller.assertPathResolvesTo<ProductDetail>("/products/42") { it.productId == "42" }
    controller.assertPathDoesNotResolve("/unknown")
    controller.assertPathFor(ProductDetail("42"), expectedPath = "/products/42")
}
// endregion
