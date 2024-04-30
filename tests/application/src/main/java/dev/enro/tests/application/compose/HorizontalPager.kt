package dev.enro.tests.application.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.core.push
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object HorizontalPager : NavigationKey.SupportsPush {
    @Parcelize
    internal class PageOne(val name: String) : NavigationKey.SupportsPush

    @Parcelize
    internal class PageTwo(val name: String) : NavigationKey.SupportsPush
}

@OptIn(ExperimentalFoundationApi::class)
@NavigationDestination(HorizontalPager::class)
@Composable
fun HorizontalPagerCrossFadeScreen() {
    val pageOne = rememberNavigationContainer(
        root = HorizontalPager.PageOne("Root"),
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept { key<HorizontalPager.PageOne>() }
    )
    val pageTwo = rememberNavigationContainer(
        root = HorizontalPager.PageTwo("Root"),
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept { key<HorizontalPager.PageTwo>() }
    )
    val containerGroup = rememberNavigationContainerGroup(pageOne, pageTwo)
    val state = rememberPagerState { containerGroup.containers.size }
    LaunchedEffect(containerGroup.activeContainer) {
        val target = when (containerGroup.activeContainer) {
            pageOne -> 0
            else -> 1
        }
        if (target != state.currentPage) {
            state.animateScrollToPage(target)
        }
    }
    HorizontalPager(state = state) { page ->
        containerGroup.containers[page].Render()
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
                navigation.push(HorizontalPager.PageOne((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (one)")
        }
        Button(
            onClick = {
                navigation.push(HorizontalPager.PageTwo((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
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
                navigation.push(HorizontalPager.PageOne((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (one)")
        }
        Button(
            onClick = {
                navigation.push(HorizontalPager.PageTwo((navigation.key.name.toIntOrNull() ?: 0).plus(1).toString()))
            }
        ) {
            Text(text = "Next Page (two)")
        }
    }
}


