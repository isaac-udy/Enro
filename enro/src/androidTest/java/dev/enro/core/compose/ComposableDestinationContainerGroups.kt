package dev.enro.core.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.setActive
import dev.enro.core.destinations.launchComposable
import dev.enro.expectComposableContext
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import org.junit.Rule
import org.junit.Test

class ComposableDestinationContainerGroups {

    @get:Rule
    val composeContentRule = createComposeRule()

    @Test
    fun whenComposableDestinationIsLaunchedWithContainerGroup_thenContainerGroupsAreSelectable() {
        val root = launchComposable(Destinations.RootDestination)
        expectComposableContext<Destinations.FirstTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Second Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()

        composeContentRule.onNodeWithTag("BottomNavigationItem_1")
            .performClick()

        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.SecondTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Second Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()

        composeContentRule.onNodeWithTag("BottomNavigationItem_2")
            .performClick()

        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.ThirdTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Second Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Third Tab Screen").assertExists()

        composeContentRule.onNodeWithTag("BottomNavigationItem_0")
            .performClick()

        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.FirstTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Second Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()
    }

    @Test
    fun whenComposableDestinationIsLaunchedWithContainerGroup_andBackButtonIsPressed_thenContainerEmptyBehaviorIsRespected() {
        val root = launchComposable(Destinations.RootDestination)
        expectComposableContext<Destinations.FirstTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Second Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()

        composeContentRule.onNodeWithTag("BottomNavigationItem_1")
            .performClick()

        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.SecondTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Second Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()

        Espresso.pressBack()

        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.FirstTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Second Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()
    }

    @Test
    fun whenComposableDestinationIsLaunchedWithContainerGroup_andSecondaryContainerSelected_andActivityIsRecreated_thenActiveContainerRemainsActive() {
        val root = launchComposable(Destinations.RootDestination)
        expectComposableContext<Destinations.FirstTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Second Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()

        composeContentRule.onNodeWithTag("BottomNavigationItem_1")
            .performClick()

        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.SecondTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Second Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            root.navigationContext.activity.recreate()
        }
        runBlocking { composeContentRule.awaitIdle() }
        expectComposableContext<Destinations.SecondTab>()
        composeContentRule.onNodeWithText("First Tab Screen").assertDoesNotExist()
        composeContentRule.onNodeWithText("Second Tab Screen").assertExists()
        composeContentRule.onNodeWithText("Third Tab Screen").assertDoesNotExist()
    }

    object Destinations {
        @Parcelize
        object RootDestination : NavigationKey.SupportsPresent

        @Parcelize
        object FirstTab : NavigationKey.SupportsPush

        @Parcelize
        object SecondTab : NavigationKey.SupportsPush

        @Parcelize
        object ThirdTab : NavigationKey.SupportsPush
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@NavigationDestination(ComposableDestinationContainerGroups.Destinations.RootDestination::class)
internal fun ContainerGroupsRootScreen() {

    val firstTab = rememberNavigationContainer(
        root = ComposableDestinationContainerGroups.Destinations.FirstTab,
        emptyBehavior = EmptyBehavior.CloseParent
    )

    val secondTab = rememberNavigationContainer(
        root = ComposableDestinationContainerGroups.Destinations.SecondTab,
        emptyBehavior = EmptyBehavior.Action {
            firstTab.setActive()
            true
        }
    )

    val thirdTab = rememberNavigationContainer(
        root = ComposableDestinationContainerGroups.Destinations.ThirdTab,
        emptyBehavior = EmptyBehavior.Action {
            firstTab.setActive()
            true
        }
    )

    val containerGroup = rememberNavigationContainerGroup(
        firstTab,
        secondTab,
        thirdTab
    )

    Scaffold(
        bottomBar = {
            BottomNavigation {
                containerGroup.containers.forEachIndexed { index, container ->
                    BottomNavigationItem(
                        modifier = Modifier.semantics {
                            testTag = "BottomNavigationItem_$index"
                        },
                        selected = container == containerGroup.activeContainer,
                        onClick = { container.setActive() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                            )
                        }
                    )
                }
            }
        }
    ) {
        containerGroup.activeContainer.Render()
    }
}


@Composable
@NavigationDestination(ComposableDestinationContainerGroups.Destinations.FirstTab::class)
fun FirstTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = 0.1f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "First Tab Screen")
    }
}

@Composable
@NavigationDestination(ComposableDestinationContainerGroups.Destinations.SecondTab::class)
fun SecondTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green.copy(alpha = 0.1f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Second Tab Screen")
    }
}

@Composable
@NavigationDestination(ComposableDestinationContainerGroups.Destinations.ThirdTab::class)
fun ThirdTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue.copy(alpha = 0.1f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Third Tab Screen")
    }
}