package dev.enro.tests.application.samples.recipes.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import kotlinx.serialization.Serializable

@Serializable
data class TitleAndEmoji(
    val title: String,
    val emoji: String,
)

@Serializable
object EnterTitle : NavigationKey.WithResult<TitleAndEmoji>

@Composable
@NavigationDestination(EnterTitle::class)
fun EnterTitleScreen() {
    val navigation = navigationHandle<EnterTitle>()
    var title by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What's your recipe called?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Give your recipe a memorable name",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        ExtendedFloatingActionButton(
            onClick = {
                navigation.complete(
                    TitleAndEmoji(
                        title = title.trim(),
                        emoji = "🍽️"
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            text = { Text("Continue") },
            icon = { Icon(Icons.Default.ArrowForward, contentDescription = null) }
        )
    }
}
