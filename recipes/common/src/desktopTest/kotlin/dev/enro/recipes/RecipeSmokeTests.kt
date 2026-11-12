package dev.enro.recipes

import dev.enro.recipes.animations.PerDestinationTransitionsRecipe
import dev.enro.recipes.backstack.BackStackOperationsRecipe
import dev.enro.recipes.containers.NestedContainersRecipe
import dev.enro.recipes.deeplink.AdvancedDeepLinkRecipe
import dev.enro.recipes.deeplink.BasicDeepLinkRecipe
import dev.enro.recipes.deeplink.HandleDeepLinkRecipe
import dev.enro.recipes.dialog.NavigatingFromDialogRecipe
import dev.enro.recipes.flow.FlowInViewModelRecipe
import dev.enro.recipes.interceptor.ModuleInterceptorRecipe
import dev.enro.recipes.koin.KoinViewModelRecipe
import dev.enro.recipes.koin.modular.KoinModularRecipe
import dev.enro.recipes.material.MaterialListDetailRecipe
import dev.enro.recipes.material.MaterialSupportingPaneRecipe
import dev.enro.recipes.metro.MetroModularRecipe
import dev.enro.recipes.metro.MetroViewModelRecipe
import dev.enro.recipes.modular.ModularNavigationRecipe
import dev.enro.recipes.multiplestacks.MultipleBackStacksRecipe
import dev.enro.recipes.results.ViewModelResultsRecipe
import dev.enro.recipes.saveable.SavedStateRecipe
import dev.enro.recipes.viewmodel.SharedViewModelRecipe
import kotlin.test.Test

/**
 * Checks the behaviour that each recipe's description states.
 */
class RecipeSmokeTests {

    @Test
    fun `back stack operations`() = runRecipeTest(BackStackOperationsRecipe) {
        see("Stack: Step 1")
        click("Open step 2")
        click("Open step 3")
        see("Stack: Step 1 > Step 2 > Step 3")

        click("Back to root")
        see("Stack: Step 1")

        click("Open step 2")
        click("Open step 3")
        click("Open step 4")
        click("Back to step 2")
        see("Stack: Step 1 > Step 2")

        click("Replace this step with step 102")
        see("Stack: Step 1 > Step 102")

        click("Finish: clear the stack")
        see("The steps were removed from the stack. This is the only entry.")
    }

    @Test
    fun `multiple back stacks keeps each tab's backstack`() = runRecipeTest(MultipleBackStacksRecipe) {
        click("View Post 1")
        click("Like")
        see("Likes: 1")

        click("Messages")
        see("Thread 1")
        gone("Post: post-1")

        click("Feed")
        see("Post: post-1")
        see("Likes: 1")

        click("Feed")
        see("View Post 1")
        gone("Post: post-1")
    }

    @Test
    fun `navigating from a dialog replaces the dialog`() = runRecipeTest(NavigatingFromDialogRecipe) {
        click("Open dialog")
        see("Choose an action")

        click("Replace this dialog with a screen")
        see("Opened from the dialog with closeAndReplaceWith.")
        gone("Choose an action")

        click("Back")
        see("Open dialog")
        gone("Choose an action")
    }

    @Test
    fun `navigating from a dialog opens on top of the dialog`() = runRecipeTest(NavigatingFromDialogRecipe) {
        click("Open dialog")
        click("Open a screen on top")
        see("Opened from the dialog with open.")

        // The dialog is still on the backstack underneath.
        click("Back")
        see("Choose an action")
    }

    @Test
    fun `nested containers route keys by filter`() = runRecipeTest(NestedContainersRecipe) {
        see("Note 1")
        see("Task 1")

        click("Open task 10 in the other pane")
        see("Task 10")
        see("Note 1")

        click("Open note 100 in the other pane")
        see("Note 100")
        see("Task 10")

        click("Open help over both panes")
        see("Help")
        gone("Task 10")
    }

    @Test
    fun `shared view model is one instance`() = runRecipeTest(SharedViewModelRecipe) {
        see("Host sees 1 item(s)")
        click("Add an item")
        see("Host sees 2 item(s)")

        click("Review order")
        see("Ordering 2 item(s)")
        click("Add one more")
        see("Host sees 3 item(s)")
    }

    @Test
    fun `view model receives a result`() = runRecipeTest(ViewModelResultsRecipe) {
        see("Accent colour: Blue")
        click("Change colour")
        click("Green")
        see("Accent colour: Green")
        see("Picked Green")

        click("Change colour")
        click("Cancel")
        see("Picker closed without a choice")
    }

    @Test
    fun `flow in a view model completes with every step`() = runRecipeTest(FlowInViewModelRecipe) {
        click("Create a booking")
        click("Kyoto")
        see("How many nights in Kyoto?")
        click("2")
        see("Add breakfast?")
        click("Yes")
        see("Kyoto, 2 night(s), with breakfast")
    }

    @Test
    fun `flow in a view model skips the branch`() = runRecipeTest(FlowInViewModelRecipe) {
        click("Create a booking")
        click("Lisbon")
        click("1")
        see("Lisbon, 1 night(s), no breakfast")
    }

    @Test
    fun `flow in a view model lets an earlier answer change`() = runRecipeTest(FlowInViewModelRecipe) {
        click("Create a booking")
        click("Kyoto")
        click("2")
        see("Add breakfast?")

        click("Back")
        see("How many nights in Kyoto?")
        click("Back")
        see("Where to?")

        // The flow skips the breakfast step for a one-night stay.
        click("Wellington")
        click("1")
        see("Wellington, 1 night(s), no breakfast")
    }

    @Test
    fun `module interceptor observes and blocks`() = runRecipeTest(ModuleInterceptorRecipe) {
        click("Open 'report'")
        see("Document: report")
        click("Close")
        see("Opened: report")
        see("Closed: report")

        click("Open 'restricted' (blocked by the interceptor)")
        see("Blocked: open restricted")
        gone("Document: restricted")

        click("Sign")
        click("Sign as Alex")
        see("Signed by: Alex")
    }

    @Test
    fun `saved state survives being covered`() = runRecipeTest(SavedStateRecipe) {
        click("Add one to each")
        click("Add one to each")
        see("remember: 2")
        see("rememberSaveable: 2")
        see("ViewModel property: 2")
        see("SavedStateHandle: 2")

        click("Open a screen on top")
        click("Back to counters")

        // Only plain remember state is lost while the destination is not composed.
        see("remember: 0")
        see("rememberSaveable: 2")
        see("ViewModel property: 2")
        see("SavedStateHandle: 2")
    }

    // The destinations are in other Gradle modules. The test passes only if
    // RecipesComponent contains the generated bindings of those modules.
    @Test
    fun `modular navigation crosses module boundaries`() = runRecipeTest(ModularNavigationRecipe) {
        see("In :recipes:modular:feature-home")
        click("View Profile")
        see("Profile: user-123")
        see("In :recipes:modular:feature-profile")
        click("Back")

        click("Login")
        click("Log in and go home")
        see("Home Screen")
    }

    @Test
    fun `koin view model reads its key`() = runRecipeTest(KoinViewModelRecipe) {
        click("Open ada")
        see("Ada Lovelace")
        click("Back")
        click("Open alan")
        see("Alan Turing")
    }

    @Test
    fun `koin modular features share one session`() = runRecipeTest(KoinModularRecipe) {
        see("Orders for Guest")
        click("Account")
        see("Account: Guest")
        click("Sign in as Ada")
        see("Orders for Ada")
    }

    @Test
    fun `metro view model reads its key`() = runRecipeTest(MetroViewModelRecipe) {
        click("Open ada")
        see("Ada Lovelace")
        click("Back")
        click("Open alan")
        see("Alan Turing")
    }

    @Test
    fun `metro modular features share one graph`() = runRecipeTest(MetroModularRecipe) {
        see("Orders for Guest")
        click("Account")
        see("Account: Guest")
        click("Sign in as Ada")
        see("Orders for Ada")
    }

    // The test window is wide enough for two panes.
    @Test
    fun `material list detail shows two panes`() = runRecipeTest(MaterialListDetailRecipe) {
        see("Select a message")

        click("Invoice")
        see("Message body for Invoice.")
        see("Inbox")

        // Selecting another message replaces the open message.
        click("Meeting notes")
        see("Message body for Meeting notes.")
        gone("Message body for Invoice.")

        click("Close message")
        see("Select a message")
    }

    @Test
    fun `material supporting pane shows beside the main pane`() = runRecipeTest(MaterialSupportingPaneRecipe) {
        click("Show related: Navigation")
        see("Related: Navigation")
        see("Article")

        click("Show related: Animations")
        see("Related: Animations")

        click("Hide related")
        gone("Related: Animations")
        see("Article")
    }

    @Test
    fun `per destination transitions`() = runRecipeTest(PerDestinationTransitionsRecipe) {
        click("Open with this destination's own transition")
        see("This destination sets its own open, close and predictive back transitions.")
        click("Back")
        see("Open with the container's transition")
    }

    @Test
    fun `basic deep link resolves urls`() = runRecipeTest(BasicDeepLinkRecipe) {
        click("Open /users/alice")
        see("User ID: alice")
        click("Back")

        click("Open /products/shoe-1?source=email")
        see("Product ID: shoe-1")
        see("Source: email")
        click("Back")

        click("Open /not-a-registered-path")
        see("No binding matches /not-a-registered-path")
    }

    @Test
    fun `handling a deep link opens the bound destination`() = runRecipeTest(HandleDeepLinkRecipe) {
        click("Receive /users/alice")
        see("User ID: alice")
        click("Back")

        click("Receive /products/shoe-1?source=push")
        see("Product ID: shoe-1")
        see("Source: push")
        click("Back")

        click("Receive /no-such-screen")
        see("No destination for /no-such-screen")
    }

    @Test
    fun `advanced deep link builds a synthetic backstack`() = runRecipeTest(AdvancedDeepLinkRecipe) {
        see("Tech Category")
        click("Deep link to /categories/tech/articles/deep-link-article")
        see("Article: deep-link-article")

        click("Normal launch")
        see("Tech Category")
        gone("Article: deep-link-article")
    }
}
