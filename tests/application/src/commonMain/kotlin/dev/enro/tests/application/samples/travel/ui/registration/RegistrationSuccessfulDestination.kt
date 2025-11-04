package dev.enro.tests.application.samples.travel.ui.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asBackstack
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.tests.application.samples.travel.HomeDestination
import dev.enro.tests.application.samples.travel.LoginDestination
import dev.enro.tests.application.samples.travel.data.TravelUserRepository
import dev.enro.ui.LocalNavigationContainer
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
class RegistrationSuccessfulDestination(
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String,
) : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(RegistrationSuccessfulDestination::class)
fun RegistrationSuccessfulScreen() {
    val navigation = navigationHandle<RegistrationSuccessfulDestination>()
    var showContent by remember { mutableStateOf(false) }
    val container = LocalNavigationContainer.current

    LaunchedEffect(container ) {
        // When the RegistrationSuccessfulDestination is launched,
        // we're going to update the parent container's backstack to
        // drop any of the previous registration/login destinations
        container.updateBackstack { backstack ->
            backstack
                .filterNot { instance ->
                    listOf(
                        LoginDestination::class,
                        RegistrationOverviewDestination::class,
                        RegistrationNameDestination::class,
                        RegistrationUsernameDestination::class,
                        RegistrationPasswordDestination::class
                    ).contains(instance.key::class)
                }
                .asBackstack()
        }
    }

    LaunchedEffect(Unit) {
        // Register the user
        TravelUserRepository.instance.registerUser(
            firstName = navigation.key.firstName,
            lastName = navigation.key.lastName,
            username = navigation.key.username,
            password = navigation.key.password
        )
    }

    // Animate content appearance
    LaunchedEffect(Unit) {
        delay(1)
        showContent = true
    }

    // Confetti animation
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500)),
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Celebration emoji
                    Text(
                        text = "🎉",
                        fontSize = 96.sp,
                        modifier = Modifier
                            .scale(scale.value)
                            .padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Welcome aboard!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Your TravelBuddy account is ready",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )

                    // Success card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "✅ Account created successfully",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = "You're all set to start exploring amazing destinations around the world!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // What's next section
                    Column(
                        modifier = Modifier.padding(bottom = 48.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "What you can do now:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        FeatureItem("🌍 Browse destinations worldwide")
                        FeatureItem("📸 Share your travel photos")
                        FeatureItem("💬 Connect with travelers")
                        FeatureItem("🔖 Save favorite places")
                        FeatureItem("✈️ Plan your next trip")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(500, delayMillis = 300)) +
                        slideInVertically(animationSpec = tween(500, delayMillis = 300)) { it }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { navigation.closeAndReplaceWith(HomeDestination(user = navigation.key.username)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text("Start Exploring 🚀", fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
