package dev.enro.tests.application.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object BottomNavigationMultiContainer : NavigationKey.SupportsPush {
    @Parcelize
    internal object Root : NavigationKey.SupportsPush

    @Parcelize
    internal object FirstTab : NavigationKey.SupportsPush

    @Parcelize
    internal object SecondTab : NavigationKey.SupportsPush

    @Parcelize
    internal object ThirdTab : NavigationKey.SupportsPush

    @Parcelize
    internal object ResultScreen : NavigationKey.SupportsPush.WithResult<String>
}

@Composable
@NavigationDestination(BottomNavigationMultiContainer::class)
fun BottomNavigationScreen() {
    val container = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("BottomNavigation"),
        root = BottomNavigationMultiContainer.Root,
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        container.Render()
    }
}

@Composable
@NavigationDestination(BottomNavigationMultiContainer.Root::class)
fun RootScreen() {
    val firstContainer = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("firstContainer"),
        root = BottomNavigationMultiContainer.FirstTab,
        filter = acceptNone(),
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    val secondContainer = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("secondContainer"),
        root = BottomNavigationMultiContainer.SecondTab,
        filter = acceptNone(),
        emptyBehavior = remember { EmptyBehavior.Action {
            firstContainer.setActive()
            true
        } } ,
    )

    val thirdContainer = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("thirdContainer"),
        root = BottomNavigationMultiContainer.ThirdTab,
        filter = acceptNone(),
        emptyBehavior = remember { EmptyBehavior.Action {
            firstContainer.setActive()
            true
        } },
    )

    val group = rememberNavigationContainerGroup(
        firstContainer,
        secondContainer,
        thirdContainer,
    )

    Column {
        Text("${firstContainer.hashCode()}")
        Text("${secondContainer.hashCode()}")
        Text("${thirdContainer.hashCode()}")
        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            targetState = group.activeContainer,
            label = "",
        ) {
            it.Render()
        }
        BottomNavigation {
            group.containers.forEach {
                BottomNavigationItem(
                    selected = it == group.activeContainer,
                    onClick = { it.setActive() },
                    label = { Text(it.key::class.simpleName ?: "") },
                    icon = { Text((group.containers.indexOf(it) + 1).toString()) }
                )
            }
        }
    }
}

@Composable
@NavigationDestination(BottomNavigationMultiContainer.FirstTab::class)
fun FirstTabScreen() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    val getResult = registerForNavigationResult<String> {
        result = it
    }
    TitledColumn("First Tab") {
        Text("Result: $result")
        Button(
            onClick = { getResult.push(BottomNavigationMultiContainer.ResultScreen) }
        ) { Text("Get result") }
    }
}

@Composable
@NavigationDestination(BottomNavigationMultiContainer.SecondTab::class)
fun SecondTabScreen() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    val getResult = registerForNavigationResult<String> {
        result = it
    }
    TitledColumn("Second Tab") {
        Text("Result: $result")
        Button(
            onClick = { getResult.push(BottomNavigationMultiContainer.ResultScreen) }
        ) { Text("Get result") }
    }
}

@Composable
@NavigationDestination(BottomNavigationMultiContainer.ThirdTab::class)
fun ThirdTabScreen() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    val getResult = registerForNavigationResult<String> {
        result = it
    }
    TitledColumn("Third Tab") {
        Text("Result: $result")
        Button(
            onClick = { getResult.push(BottomNavigationMultiContainer.ResultScreen) }
        ) { Text("Get result") }
    }
}

@Composable
@NavigationDestination(BottomNavigationMultiContainer.ResultScreen::class)
fun ResultScreen() {
    val navigation = navigationHandle<BottomNavigationMultiContainer.ResultScreen>()
    var text by rememberSaveable { mutableStateOf("") }
    TitledColumn("Result") {
        TextField(value = text, onValueChange = { text = it })
        Button(onClick = { navigation.closeWithResult(text) }) {
            Text("Send Result")
        }
    }
}