package dev.enro.recipes.modular.profile

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
import dev.enro.close
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.recipes.modular.navigation.HomeKey
import dev.enro.recipes.modular.navigation.LoginKey
import dev.enro.recipes.modular.navigation.ModularSettingsKey
import dev.enro.recipes.modular.navigation.ProfileKey
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination

@Composable
@NavigationDestination(ProfileKey::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ProfileKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Profile: ${navigation.key.userId}", style = MaterialTheme.typography.titleLarge)
        Text("In :recipes:modular:feature-profile", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

@Composable
@NavigationDestination(ModularSettingsKey::class)
fun SettingsScreen() {
    val navigation = navigationHandle<ModularSettingsKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings: ${navigation.key.section}", style = MaterialTheme.typography.titleLarge)
        Text("In :recipes:modular:feature-profile", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

// A destination declared as a value. It is registered the same way as the
// functions above: the annotation is all the processor needs.
@NavigationDestination(LoginKey::class)
val loginDestination: NavigationDestinationProvider<LoginKey> = navigationDestination {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Login Screen", style = MaterialTheme.typography.titleLarge)
        Text("In :recipes:modular:feature-profile", style = MaterialTheme.typography.bodySmall)
        // This module opens the home feature's screen by key, too.
        Button(onClick = { navigation.closeAndReplaceWith(HomeKey) }) {
            Text("Log in and go home")
        }
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
