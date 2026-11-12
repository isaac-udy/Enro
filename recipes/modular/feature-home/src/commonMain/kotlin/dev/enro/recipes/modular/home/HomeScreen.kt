package dev.enro.recipes.modular.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.modular.navigation.HomeKey
import dev.enro.recipes.modular.navigation.LoginKey
import dev.enro.recipes.modular.navigation.ModularSettingsKey
import dev.enro.recipes.modular.navigation.ProfileKey

// This module cannot see ProfileScreen, SettingsScreen or the login
// destination: they are in :recipes:modular:feature-profile, which this
// module does not depend on. It opens them by key.
@Composable
@NavigationDestination(HomeKey::class)
fun HomeScreen() {
    val navigation = navigationHandle<HomeKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home Screen", style = MaterialTheme.typography.titleLarge)
        Text("In :recipes:modular:feature-home", style = MaterialTheme.typography.bodySmall)

        Button(onClick = { navigation.open(ProfileKey("user-123")) }) {
            Text("View Profile")
        }
        Button(onClick = { navigation.open(ModularSettingsKey()) }) {
            Text("Settings")
        }
        Button(onClick = { navigation.open(LoginKey) }) {
            Text("Login")
        }
    }
}
