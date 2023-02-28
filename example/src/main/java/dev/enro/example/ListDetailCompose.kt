package dev.enro.example

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.*
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class ListDetailComposeKey : NavigationKey.SupportsPush

@Parcelize
class ListComposeKey : NavigationKey.SupportsPush

@Parcelize
class DetailComposeKey(
    val id: String
) : NavigationKey.SupportsPush

val listContainerKey = NavigationContainerKey.FromName("listContainerKey")
val detailContainerKey = NavigationContainerKey.FromName("detailContainerKey")

@Composable
@NavigationDestination(ListDetailComposeKey::class)
fun ListDetailComposeScreen() {
    val listContainerController = rememberNavigationContainer(
        root = ListComposeKey(),
        key = listContainerKey,
        emptyBehavior = EmptyBehavior.CloseParent,
        accept = { it is ListComposeKey }
    )
    val detailContainerController = rememberNavigationContainer(
        key = detailContainerKey,
        emptyBehavior = EmptyBehavior.AllowEmpty,
        accept = { it is DetailComposeKey }
    )
    
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Row(
            modifier = Modifier.background(MaterialTheme.colors.background)
        ) {
            Box(
                modifier = Modifier.weight(1f, true)
            ) {
                listContainerController.Render()
            }
            Box(
                modifier = Modifier.weight(1f, true)
            ) {
                detailContainerController.Render()
            }
        }
    } else {
        Box(
            modifier = Modifier.background(MaterialTheme.colors.background)
        ) {
            listContainerController.Render()
            detailContainerController.Render()
        }
    }
}

@Composable
@NavigationDestination(ListComposeKey::class)
fun ListComposeScreen() {
    val items = rememberSaveable {
        List(100) { UUID.randomUUID().toString() }
    }
    val navigation = navigationHandle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            Text(
                text = item,
                modifier = Modifier
                    .clickable {
                        navigation.onContainer(detailContainerKey) {
                            setBackstack {
                                emptyBackstack()
                                    .push(DetailComposeKey(item))
                            }
                        }
                    }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
@NavigationDestination(DetailComposeKey::class)
fun DetailComposeScreen() {
    val navigation = navigationHandle<DetailComposeKey>()

    Box(
        modifier = Modifier
        .background(Color.White)
        .fillMaxSize()
    ) {
        Text(
            text = navigation.key.id, modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}

