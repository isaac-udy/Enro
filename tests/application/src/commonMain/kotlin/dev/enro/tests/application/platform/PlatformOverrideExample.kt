package dev.enro.tests.application.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import kotlinx.serialization.Serializable

@Serializable
object PlatformOverrideExample : NavigationKey

@Composable
@NavigationDestination(PlatformOverrideExample::class)
fun PlatformOverrideExample() {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Platform Override Example\nCommon Destination",
            style = MaterialTheme.typography.subtitle1,
        )
    }
}