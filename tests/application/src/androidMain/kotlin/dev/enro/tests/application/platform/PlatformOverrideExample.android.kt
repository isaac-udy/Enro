package dev.enro.tests.application.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.enro.annotations.NavigationDestination
import dev.enro.asCommonDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.open


@Composable
@NavigationDestination.PlatformOverride(PlatformOverrideExample::class)
fun PlatformOverrideExampleForAndroid() {
    val navigation = navigationHandle()
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Platform Override Example\nAndroid Destination",
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = {
                navigation.open(PlatformOverrideExample.asCommonDestination())
            }
        ) {
            Text(text = "Open Common Destination")
        }
    }
}