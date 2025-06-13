package dev.enro.tests.application.samples.travel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
object EnterUsernameScreen : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(EnterUsernameScreen::class)
fun EnterUsernameScreenDestination() {
    val navigation = navigationHandle<EnterUsernameScreen>()
    var username by rememberSaveable { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf(false) }

    // Simulate username availability check
    LaunchedEffect(username) {
        if (username.length >= 3) {
            isChecking = true
            delay(500) // Simulate API call
            isAvailable = !listOf("admin", "user", "test", "traveler").contains(username.lowercase())
            isChecking = false
        } else {
            isAvailable = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navigation.close() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
            )

            // Header
            Text(
                text = "🧳",
                fontSize = 56.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Choose your traveler ID",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "This is how other travelers will find you",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().replace(" ", "") },
                label = { Text("Username") },
                placeholder = { Text("e.g., wanderlust123") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                isError = username.length >= 3 && !isChecking && !isAvailable,
                trailingIcon = {
                    when {
                        isChecking -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                        username.length >= 3 && isAvailable -> Icon(
                            Icons.Default.Check,
                            contentDescription = "Available",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // Availability message
            when {
                username.length < 3 && username.isNotEmpty() -> {
                    Text(
                        text = "Username must be at least 3 characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                isChecking -> {
                    Text(
                        text = "Checking availability...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                username.length >= 3 && isAvailable -> {
                    Text(
                        text = "✅ Great choice! This username is available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                username.length >= 3 && !isAvailable -> {
                    Text(
                        text = "❌ This username is already taken",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                else -> {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // Username tips
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡 Username tips:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "• Use letters and numbers only\n• Make it memorable and unique\n• You can't change it later",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            Button(
                onClick = { navigation.open(EnterPasswordScreen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = username.length >= 3 && isAvailable && !isChecking
            ) {
                Text("Continue", fontSize = 18.sp)
            }

            Text(
                text = "Step 2 of 4",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )
        }
    }
}