/**
 * Enro Recipe: Modular Navigation
 *
 * Nav3 equivalent: "Hilt Modularized" recipe
 * https://nicbell.github.io/nav3/recipes/hilt-modularized
 *
 * Demonstrates how Enro handles navigation across feature modules without requiring
 * a DI framework, compared to Nav3's Hilt-based approach.
 *
 * Key differences from Nav3:
 * - Nav3 uses Hilt multibinding to collect entryProviders from different modules and
 *   combine them into a single entryProvider for NavDisplay.
 * - Enro uses KSP code generation to create NavigationModule classes at compile time.
 *   Each module generates its own NavigationModule, and they are composed together
 *   when creating the NavigationController.
 * - No DI framework is required. Enro's modular navigation works with or without Hilt/Koin/etc.
 * - NavigationKeys are defined in shared/API modules, while destinations are defined in
 *   feature modules. The framework handles the binding.
 *
 * Project structure for modular navigation:
 *
 * :app                    - Composes all NavigationModules
 * :core:navigation        - Shared NavigationKey definitions
 * :feature:home           - HomeScreen destination + its NavigationModule
 * :feature:profile        - ProfileScreen destination + its NavigationModule
 * :feature:settings       - SettingsScreen destination + its NavigationModule
 */
package dev.enro.recipes.modular

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationModule
import dev.enro.controller.createNavigationModule
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.path.NavigationPathBinding
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable

// ============================================================
// :core:navigation module - Shared NavigationKey definitions
// ============================================================
// These keys are the navigation contracts. They live in a shared module
// that all feature modules depend on.

// File: core/navigation/src/main/kotlin/com/example/navigation/Keys.kt
@Serializable
data object HomeKey : NavigationKey

@Serializable
data class ProfileKey(val userId: String) : NavigationKey

@Serializable
data class SettingsKey(val section: String = "general") : NavigationKey

@Serializable
data object LoginKey : NavigationKey

// ============================================================
// :feature:home module - Home destination
// ============================================================
// The KSP processor generates a NavigationModule for this module automatically.
// The generated module registers the binding: HomeKey::class -> HomeScreen composable.

// File: feature/home/src/main/kotlin/com/example/home/HomeScreen.kt

@Composable
@NavigationDestination(HomeKey::class)
fun HomeScreen() {
    val navigation = navigationHandle<HomeKey>()
    Column {
        Text("Home Screen")
        // This module knows about ProfileKey and SettingsKey (from :core:navigation)
        // but does NOT need to know about their implementations.
        Button(onClick = { navigation.open(ProfileKey("user-123")) }) {
            Text("View Profile")
        }
        Button(onClick = { navigation.open(SettingsKey()) }) {
            Text("Settings")
        }
    }
}

// ============================================================
// :feature:profile module - Profile destination
// ============================================================
// File: feature/profile/src/main/kotlin/com/example/profile/ProfileScreen.kt

@Composable
@NavigationDestination(ProfileKey::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ProfileKey>()
    Column {
        Text("Profile: ${navigation.key.userId}")
    }
}

// ============================================================
// :feature:settings module - Settings destination
// ============================================================
// File: feature/settings/src/main/kotlin/com/example/settings/SettingsScreen.kt

@Composable
@NavigationDestination(SettingsKey::class)
fun SettingsScreen() {
    val navigation = navigationHandle<SettingsKey>()
    Column {
        Text("Settings: ${navigation.key.section}")
    }
}

// ============================================================
// Manual NavigationModule (when you need more control)
// ============================================================
// You can also create NavigationModules manually for registering interceptors,
// path bindings, plugins, and destinations that aren't annotation-based.

// File: feature/auth/src/main/kotlin/com/example/auth/AuthModule.kt

@NavigationDestination(LoginKey::class)
val loginDestination: NavigationDestinationProvider<LoginKey> = navigationDestination {
    val navigation = navigationHandle<LoginKey>()
    Column {
        Text("Login Screen")
    }
}

val authModule: NavigationModule = createNavigationModule {
    // Register a destination manually
    destination(loginDestination)

    // Register a path binding
    path(
        NavigationPathBinding(
            keyType = ProfileKey::class,
            pattern = "/profile/{userId}",
            deserialize = { ProfileKey(userId = get("userId")) },
            serialize = { set("userId", it.userId) },
        )
    )

    // Register an interceptor
    interceptor(navigationInterceptor {
        onOpened<SettingsKey> {
            // Example: log analytics for settings access
            continueWithOpen()
        }
    })
}

// ============================================================
// :app module - Compose all modules
// ============================================================
// The app module brings together all feature modules' NavigationModules.
// With KSP, each module generates a NavigationModule automatically.
// You compose them when creating the NavigationController.

// File: app/src/main/kotlin/com/example/app/AppNavigation.kt

// KSP generates these classes based on @NavigationDestination annotations:
// - com.example.home.GeneratedNavigationModule
// - com.example.profile.GeneratedNavigationModule
// - com.example.settings.GeneratedNavigationModule

val appModule: NavigationModule = createNavigationModule {
    // Include KSP-generated modules from each feature module.
    // In practice, these are auto-discovered via the generated code.
    // Here we show the manual composition for clarity.

    // module(homeGeneratedNavigationModule)
    // module(profileGeneratedNavigationModule)
    // module(settingsGeneratedNavigationModule)

    // Include manually-created modules
    module(authModule)
}

// Then in your Application class or entry point:
//
// val controller = createEnroController {
//     module(appModule)
// }
//
// Nav3 equivalent (with Hilt):
//   @Module @InstallIn(SingletonComponent::class)
//   interface NavModule {
//       @Binds @IntoSet fun homeEntryProvider(impl: HomeEntryProvider): EntryProvider
//       @Binds @IntoSet fun profileEntryProvider(impl: ProfileEntryProvider): EntryProvider
//       ...
//   }
//   Then in NavDisplay: NavDisplay(backStack, entryProviders = entryProviders)
//
// Enro's approach is simpler: no DI annotations, no interface implementations,
// just @NavigationDestination on your composables and KSP handles the wiring.
