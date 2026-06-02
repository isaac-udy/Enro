package dev.enro.tests.application.compose

import android.os.Parcelable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.enro.NavigationOperation
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationKey
import dev.enro.core.asPush
import dev.enro.core.closeWithResult
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone
import dev.enro.core.container.backstackOf
import dev.enro.core.container.setBackstack
import dev.enro.core.push
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object BottomNavigation : Parcelable, NavigationKey.SupportsPush {
    @Parcelize
    internal object Root : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object MutliContainer : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object SingleContainerReplace : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object SingleContainerBackstackManipulation : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object FirstTab : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object SecondTab : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object ThirdTab : Parcelable, NavigationKey.SupportsPush

    @Parcelize
    internal object ResultScreen : Parcelable, NavigationKey.SupportsPush.WithResult<String>
}

@Composable
@NavigationDestination(BottomNavigation::class)
fun BottomNavigationScreen() {
    val container = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("BottomNavigation"),
        root = BottomNavigation.Root,
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        container.Render()
    }
}

@Composable
@NavigationDestination(BottomNavigation.Root::class)
fun RootScreen() {
    val navigation = navigationHandle<BottomNavigation.Root>()
    TitledColumn("Bottom Navigation") {
        Button(onClick = { navigation.push(BottomNavigation.MutliContainer) }) {
            Text("Multi Container")
        }
        Button(onClick = { navigation.push(BottomNavigation.SingleContainerReplace) }) {
            Text("Single Container Replace")
        }
        Button(onClick = { navigation.push(BottomNavigation.SingleContainerBackstackManipulation) }) {
            Text("Single Container Backstack Manipulation")
        }
    }
}

@Composable
@NavigationDestination(BottomNavigation.MutliContainer::class)
fun MultiContainerBottomNavigationScreen() {
    val firstContainer = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("FirstTab"),
        root = BottomNavigation.FirstTab,
        filter = acceptNone(),
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    val secondContainer = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("SecondTab"),
        root = BottomNavigation.SecondTab,
        filter = acceptNone(),
        emptyBehavior = EmptyBehavior.Action {
//            firstContainer.setActive()
            TODO("set active")
            true
        },
    )

    val thirdContainer = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("ThirdTab"),
        root = BottomNavigation.ThirdTab,
        filter = acceptNone(),
        emptyBehavior = EmptyBehavior.Action {
//            firstContainer.setActive()
            TODO("set active")
            true
        },
    )

    val group = rememberNavigationContainerGroup(
        firstContainer,
        secondContainer,
        thirdContainer,
    )

    Column {
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
                    onClick = { group.setActive(it) },
                    label = { Text(it.key.name) },
                    icon = { Text((group.containers.indexOf(it) + 1).toString()) }
                )
            }
        }
    }
}

@Composable
@NavigationDestination(BottomNavigation.SingleContainerReplace::class)
fun SingleContainerReplaceBottomNavigation() {
    val container = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("firstContainer"),
        root = BottomNavigation.FirstTab,
        filter = acceptNone(),
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    Column {
        Box(modifier = Modifier.weight(1f)) {
            container.Render()
        }
        BottomNavigation {
            listOf(
                BottomNavigation.FirstTab,
                BottomNavigation.SecondTab,
                BottomNavigation.ThirdTab,
            ).forEachIndexed { index, it ->
                val activeKey = container.backstack.firstOrNull()?.key
                BottomNavigationItem(
                    selected = activeKey == it,
                    onClick = {
                        if (activeKey != it) {
                            container.setBackstack(backstackOf(it.asPush()))
                        }
                    },
                    label = { Text(it::class.simpleName ?: "") },
                    icon = { Text((index + 1).toString()) }
                )
            }
        }
    }
}

@Composable
@NavigationDestination(BottomNavigation.SingleContainerBackstackManipulation::class)
fun SingleContainerBackstackManipulationBottomNavigation() {
    val container = rememberNavigationContainer(
        key = NavigationContainerKey.FromName("firstContainer"),
        root = BottomNavigation.FirstTab,
        filter = acceptNone(),
        emptyBehavior = EmptyBehavior.CloseParent,
        animations = {},
    )

    Column {
        Box(modifier = Modifier.weight(1f)) {
            container.Render()
        }
        BottomNavigation {
            val tabs = listOf(
                BottomNavigation.FirstTab,
                BottomNavigation.SecondTab,
                BottomNavigation.ThirdTab,
            )
            tabs.forEachIndexed { index, tabKey ->
                val activeKey = container.backstack.lastOrNull()?.key
                val backstack = container.backstack
                BottomNavigationItem(
                    selected = activeKey == tabKey,
                    onClick = {
                        val matchingInstruction = backstack.firstOrNull { it.key == tabKey }
                            ?: tabKey.asPush()
                        container.execute(NavigationOperation.Open(matchingInstruction))
                    },
                    label = { Text(tabKey::class.simpleName ?: "") },
                    icon = { Text((index + 1).toString()) }
                )
            }
        }
    }
}


@Composable
@NavigationDestination(BottomNavigation.FirstTab::class)
fun FirstTabScreen() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    val getResult = registerForNavigationResult<String> {
        result = it
    }
    TitledColumn("First Tab") {
        Text("Result: $result")
        Button(
            onClick = { getResult.push(BottomNavigation.ResultScreen) }
        ) { Text("Get result") }
    }
}

@Composable
@NavigationDestination(BottomNavigation.SecondTab::class)
fun SecondTabScreen() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    val getResult = registerForNavigationResult<String> {
        result = it
    }
    TitledColumn("Second Tab") {
        Text("Result: $result")
        Button(
            onClick = { getResult.push(BottomNavigation.ResultScreen) }
        ) { Text("Get result") }
    }
}

@Composable
@NavigationDestination(BottomNavigation.ThirdTab::class)
fun ThirdTabScreen() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    val getResult = registerForNavigationResult<String> {
        result = it
    }
    TitledColumn("Third Tab") {
        Text("Result: $result")
        Button(
            onClick = { getResult.push(BottomNavigation.ResultScreen) }
        ) { Text("Get result") }
    }
}

@Composable
@NavigationDestination(BottomNavigation.ResultScreen::class)
fun ResultScreen() {
    val navigation = navigationHandle<BottomNavigation.ResultScreen>()
    var text by rememberSaveable { mutableStateOf("") }
    TitledColumn("Result") {
        TextField(
            value = text,
            onValueChange = { text = it }
        )
        Button(onClick = { navigation.closeWithResult(text) }) {
            Text("Send Result")
        }
    }
}