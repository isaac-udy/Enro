package dev.enro.tests.application.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.LocalNavigationContext
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object HorizontalPager : NavigationKey {
    @Serializable
    internal class PageOne(val name: String) : NavigationKey

    @Serializable
    internal class PageTwo(val name: String) : NavigationKey
}

@OptIn(ExperimentalFoundationApi::class)
@NavigationDestination(HorizontalPager::class)
@Composable
fun HorizontalPagerCrossFadeScreen() {
    val parentContext = LocalNavigationContext.current
    val pageOne = rememberNavigationContainer(
        key = NavigationContainer.Key("Page One Container"),
        backstack = listOf(HorizontalPager.PageOne("Root").asInstance()),
        filter = accept { key<HorizontalPager.PageOne>() }
    )
    val pageTwo = rememberNavigationContainer(
        key = NavigationContainer.Key("Page Two Container"),
        backstack = listOf(HorizontalPager.PageTwo("Root").asInstance()),
        filter = accept { key<HorizontalPager.PageTwo>() }
    )
    val state = rememberPagerState { 2 }
    LaunchedEffect(parentContext.activeChild) {
        val target = when {
            pageOne.context.isActive -> 0
            else -> 1
        }
        if (target != state.currentPage) {
            state.animateScrollToPage(target)
        }
    }
    Column(modifier = Modifier
        .background(MaterialTheme.colors.background)
        .fillMaxSize()
    ) {
        Row {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextButton(
                    onClick = {
                        pageOne.context.requestActive()
                    },
                ) {
                    Text(text = "Page One")
                }
                if (pageOne.context.isActive) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colors.primary)
                            .fillMaxWidth()
                            .height(3.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextButton(
                    onClick = {
                        pageTwo.context.requestActive()
                    },
                ) {
                    Text(text = "Page Two")
                }
                if (pageTwo.context.isActive) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colors.primary)
                            .fillMaxWidth()
                            .height(3.dp)
                    )
                }
            }
        }
        HorizontalPager(
            state = state,
            modifier = Modifier.weight(1f),
        ) { page ->
            NavigationDisplay(
                when (page) {
                    0 -> pageOne
                    else -> pageTwo
                }
            )
        }
    }
}

@NavigationDestination(HorizontalPager.PageOne::class)
@Composable
fun HorizontalPagerCrossFadePageOneScreen() {
    val navigation = navigationHandle<HorizontalPager.PageOne>()
    TitledColumn(title = "Page One") {
        Text(text = "Id: ${navigation.key.name}")
        Button(
            onClick = {
                navigation.open(HorizontalPager.PageOne((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (one)")
        }
        Button(
            onClick = {
                navigation.open(HorizontalPager.PageTwo((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (two)")
        }
    }
}


@NavigationDestination(HorizontalPager.PageTwo::class)
@Composable
fun HorizontalPagerCrossFadePageTwoScreen() {
    val navigation = navigationHandle<HorizontalPager.PageTwo>()
    TitledColumn(title = "Page Two") {
        Text(text = "Id: ${navigation.key.name}")
        Button(
            onClick = {
                navigation.open(HorizontalPager.PageOne((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (one)")
        }
        Button(
            onClick = {
                navigation.open(HorizontalPager.PageTwo((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (two)")
        }
    }
}


