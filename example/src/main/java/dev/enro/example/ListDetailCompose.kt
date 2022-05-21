package dev.enro.example

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberEnroContainerController
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.replace
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class ListDetailComposeKey : NavigationKey

@Parcelize
class ListComposeKey : NavigationKey

@Parcelize
class DetailComposeKey(
    val id: String
) : NavigationKey


@Composable
@NavigationDestination(ListDetailComposeKey::class)
fun MasterDetailComposeScreen() {
    val listContainerController = rememberEnroContainerController(
        initialBackstack = listOf(NavigationInstruction.Forward(ListComposeKey())),
        emptyBehavior = EmptyBehavior.CloseParent,
        accept = { it is ListComposeKey }
    )
    val detailContainerController = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.AllowEmpty,
        accept = { it is DetailComposeKey }
    )
    
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Row {
            EnroContainer(
                controller = listContainerController,
                modifier = Modifier.weight(1f, true),
            )
            EnroContainer(
                controller = detailContainerController,
                modifier = Modifier.weight(1f, true)
            )
        }
    } else {
        Box {
            EnroContainer(controller = listContainerController)
            EnroContainer(controller = detailContainerController)
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
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        items.forEach {
            Text(
                text = it,
                modifier = Modifier
                    .clickable {
                        navigation.replace(DetailComposeKey(it))
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

