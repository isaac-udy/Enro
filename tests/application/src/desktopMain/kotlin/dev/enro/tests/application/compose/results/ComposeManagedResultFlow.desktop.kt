package dev.enro.tests.application.compose.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import dev.enro.annotations.PlatformDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle

//@NavigationDestination(ComposeManagedResultFlow.PresentedResult::class)
@PlatformDestination
@Composable
fun PresentedResultScreenForDesktop() {
    val navigation = navigationHandle<ComposeManagedResultFlow.PresentedResult>()
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
    )
    DialogWindow(
        title = "",
        resizable = false,
        onCloseRequest = { navigation.close() },
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                Modifier
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Presented",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navigation.complete("A") }) {
                    Text("Continue (A)")
                }

                Button(onClick = { navigation.complete("B") }) {
                    Text("Continue (B)")
                }
            }
        }
    }
}
