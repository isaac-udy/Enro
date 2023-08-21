package dev.enro.example.destinations.listdetail.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.destination.compose.rememberNavigationContainer
import dev.enro.core.container.*
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class ListDetailComposable : NavigationKey.SupportsPush

val listContainerKey = NavigationContainerKey.FromName("listContainerKey")
val detailContainerKey = NavigationContainerKey.FromName("detailContainerKey")

@Composable
@NavigationDestination(ListDetailComposable::class)
fun ListDetailComposeScreen() {
    val listContainerController = rememberNavigationContainer(
        root = ListComposable(),
        key = listContainerKey,
        emptyBehavior = EmptyBehavior.CloseParent,
        accept = { it is ListComposable }
    )
    val detailContainerController = rememberNavigationContainer(
        key = detailContainerKey,
        emptyBehavior = EmptyBehavior.AllowEmpty,
        accept = { it is DetailComposable }
    )
    
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier.weight(1f, true)
                    .clipToBounds()
            ) {
                listContainerController.Render()
            }
            Box(
                modifier = Modifier.weight(1f, true)
                    .clipToBounds()
            ) {
                detailContainerController.Render()
            }
        }
    } else {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize()
        ) {
            listContainerController.Render()
            detailContainerController.Render()
        }
    }
}

