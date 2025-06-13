package dev.enro.tests.application.samples.travel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.tests.application.samples.travel.domain.TravelDestination
import kotlinx.serialization.Serializable

@Serializable
class Home(
    val user: String,
) : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NavigationDestination(Home::class)
fun HomeDestination() {
    val navigation = navigationHandle<Home>()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val travelDestinations = remember {
        listOf(
            TravelDestination("🗼", "Paris", "City of lights and romance", "★★★★★"),
            TravelDestination("🏝️", "Bali", "Tropical paradise awaits", "★★★★☆"),
            TravelDestination("🗽", "New York", "The city that never sleeps", "★★★★★"),
            TravelDestination("🏔️", "Swiss Alps", "Mountain adventure calling", "★★★★☆"),
            TravelDestination("🏛️", "Rome", "Ancient history comes alive", "★★★★★"),
            TravelDestination("🌸", "Tokyo", "Modern meets traditional", "★★★★☆"),
            TravelDestination("🏖️", "Maldives", "Crystal clear waters", "★★★★★"),
            TravelDestination("🦁", "Safari Kenya", "Wildlife adventure", "★★★★☆"),
            TravelDestination("🌉", "San Francisco", "Golden Gate beauty", "★★★★☆"),
            TravelDestination("🏰", "Edinburgh", "Medieval charm", "★★★★☆"),
            TravelDestination("🎭", "Rio de Janeiro", "Carnival and beaches", "★★★★★"),
            TravelDestination("🕌", "Istanbul", "Where continents meet", "★★★★☆")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Flight,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("TravelBuddy")
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Welcome section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome back, Explorer! 🌍",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Where will your next adventure take you?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Destinations grid
            Text(
                text = "Popular Destinations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(travelDestinations) { destination ->
                    DestinationCard(destination = destination)
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Ready to leave? 😢") },
            text = { Text("Are you sure you want to log out of TravelBuddy?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        navigation.closeAndReplaceWith(LoginScreen)
                    }
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }
}

@Composable
fun DestinationCard(destination: TravelDestination) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        onClick = { /* Could navigate to destination details */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = destination.emoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = destination.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = destination.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = destination.rating,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}