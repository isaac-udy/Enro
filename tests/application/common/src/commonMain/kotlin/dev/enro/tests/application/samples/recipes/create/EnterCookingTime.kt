package dev.enro.tests.application.samples.recipes.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import kotlinx.serialization.Serializable

@Serializable
object EnterCookingTime : NavigationKey.WithResult<Int>

@Composable
@NavigationDestination(EnterCookingTime::class)
fun EnterCookingTimeScreen() {
    val navigation = navigationHandle<EnterCookingTime>()
    var cookingTime by remember { mutableStateOf("") }

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
                text = "How long does it take?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter the total cooking time in minutes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = cookingTime,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        cookingTime = newValue
                    }
                },
                label = { Text("Cooking time (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = {
                    if (cookingTime.isNotBlank()) {
                        val minutes = cookingTime.toIntOrNull() ?: 0
                        val hours = minutes / 60
                        val remainingMinutes = minutes % 60
                        val timeString = when {
                            hours > 0 && remainingMinutes > 0 -> "$hours hour${if (hours > 1) "s" else ""} $remainingMinutes min"
                            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
                            else -> "$minutes min"
                        }
                        Text(timeString)
                    }
                }
            )
        }

        ExtendedFloatingActionButton(
            onClick = {
                navigation.complete(cookingTime.toIntOrNull() ?: 0)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            text = { Text("Continue") },
            icon = { Icon(Icons.Default.ArrowForward, contentDescription = null) }
        )
    }
}
