package dev.enro.tests.application.samples.travel.ui.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import kotlinx.serialization.Serializable

@Serializable
class RegistrationPasswordDestination(
    val firstName: String,
    val lastName: String,
    val username: String,
) : NavigationKey

data class PasswordRequirement(
    val label: String,
    val check: (String) -> Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(RegistrationPasswordDestination::class)
fun EnterPasswordScreenDestination() {
    val navigation = navigationHandle<RegistrationPasswordDestination>()
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val requirements = remember {
        listOf(
            PasswordRequirement("At least 6 characters") { it.length >= 6 },
            PasswordRequirement("Contains uppercase letter") { it.any { c -> c.isUpperCase() } },
            PasswordRequirement("Contains lowercase letter") { it.any { c -> c.isLowerCase() } },
            PasswordRequirement("Contains a number") { it.any { c -> c.isDigit() } },
            PasswordRequirement("Contains special character") { it.any { c -> !c.isLetterOrDigit() } }
        )
    }

    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val threePlusRequirementsMet = requirements.count { it.check(password) } >= 3

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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { 0.75f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, top = 24.dp),
            )

            // Header
            Text(
                text = "🔐",
                fontSize = 56.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Secure your account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Choose a strong password to keep your travels safe",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            // Password match indicator
            Box(
                modifier = Modifier
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (confirmPassword.isNotEmpty()) {
                    Text(
                        text = if (passwordsMatch) "✅ Passwords match" else "❌ Passwords don't match",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (passwordsMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Password requirements
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
                        text = "Password requirements (3/5 required):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    requirements.forEach { requirement ->
                        RequirementRow(
                            label = requirement.label,
                            isMet = requirement.check(password)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            Button(
                onClick = {
                    navigation.open(
                        RegistrationSuccessfulDestination(
                            firstName = navigation.key.firstName,
                            lastName = navigation.key.lastName,
                            username = navigation.key.username,
                            password = password
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = threePlusRequirementsMet && passwordsMatch
            ) {
                Text("Create Account", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RequirementRow(label: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer.copy(
                alpha = 0.6f
            ),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer.copy(
                alpha = 0.6f
            )
        )
    }
}
