package dev.enro.example

import android.os.Bundle
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
import dev.enro.annotations.NavigationDestination
import dev.enro.core.*
import dev.enro.core.compose.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExampleLockedScreenKey(
    val previousState: Bundle
) : NavigationKey.SupportsPush

@NavigationDestination(ExampleLockedScreenKey::class)
@Composable
fun ExampleLockedScreen() {
    val navigation = navigationHandle<ExampleLockedScreenKey>()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Button(onClick = {
            navigation.present(RestoreRootState(navigation.key.previousState))
        }) {
            Text("Unlock")
        }
    }
}