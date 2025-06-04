package dev.enro.tests.application.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
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
    val pageOne = rememberNavigationContainer(
        backstack = listOf(HorizontalPager.PageTwo("Root").asInstance()),
        filter = accept { key<HorizontalPager.PageOne>() }
    )
    val pageTwo = rememberNavigationContainer(
        backstack = listOf(HorizontalPager.PageTwo("Root").asInstance()),
        filter = accept { key<HorizontalPager.PageTwo>() }
    )
    val state = rememberPagerState { 2 }
//    LaunchedEffect(containerGroup.activeContainer) {
//        val target = when (containerGroup.activeContainer) {
//            pageOne -> 0
//            else -> 1
//        }
//        if (target != state.currentPage) {
//            state.animateScrollToPage(target)
//        }
//    }
    HorizontalPager(state = state) { page ->
        NavigationDisplay(
            when (page) {
                0 -> pageOne
                else -> pageTwo
            }
        )
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


