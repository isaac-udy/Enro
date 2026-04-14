/**
 * Enro Recipe: Fragment Interop
 *
 * Nav3 equivalent: "Fragment Interop" recipe
 * https://nicbell.github.io/nav3/recipes/fragment-interop
 *
 * Demonstrates how Enro supports Fragment-based destinations alongside Compose destinations.
 *
 * Key differences from Nav3:
 * - Nav3's Fragment interop uses AndroidViewBinding or AndroidFragment within a composable
 *   entryProvider to embed Fragment content.
 * - Enro has first-class Fragment support via the enro-compat module. Fragments can be
 *   registered as destinations using @NavigationDestination, just like Composables.
 * - Enro allows seamless navigation between Fragment and Compose destinations.
 *   A Compose screen can open a Fragment destination and vice versa.
 * - Fragment destinations participate in the same navigation system (backstacks, results,
 *   interceptors) as Compose destinations.
 *
 * Note: Fragment support is in the enro-compat module, which provides Android-specific
 * compatibility. The core Enro runtime is multiplatform (Compose-based).
 */
package dev.enro.recipes.interop

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// -- Navigation Keys --
// Fragment destinations use @Parcelize for Android compatibility.
// They can also be @Serializable if needed for deep linking.

@Parcelize
@Serializable
data object FragmentHome : NavigationKey, Parcelable

@Parcelize
@Serializable
data class LegacyDetail(val itemId: String) : NavigationKey, Parcelable

@Parcelize
@Serializable
data object ComposeScreen : NavigationKey, Parcelable

// ============================================================
// Fragment Destination
// ============================================================
// Enro supports Fragment destinations via @NavigationDestination on the Fragment class.
// The fragment receives its NavigationKey through the navigation handle.
//
// Nav3 equivalent: Embedding a Fragment via AndroidFragment() in the entryProvider.
// In Nav3, you'd need to manually pass the key to the Fragment via Bundle arguments.
// Enro handles this automatically.

@NavigationDestination(FragmentHome::class)
class HomeFragment : Fragment() {

    // navigationHandle() in a Fragment works the same as in a Composable.
    // It provides typed access to the NavigationKey and navigation operations.
    private val navigation by navigationHandle<FragmentHome>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(context).apply {
                text = "Fragment Home Screen"
            })

            // Navigate to another Fragment destination
            addView(Button(context).apply {
                text = "View Legacy Detail"
                setOnClickListener {
                    navigation.open(LegacyDetail("item-from-fragment"))
                }
            })

            // Navigate from Fragment to Compose destination
            // This works seamlessly -- the navigation system doesn't care about
            // the destination type.
            addView(Button(context).apply {
                text = "Open Compose Screen"
                setOnClickListener {
                    navigation.open(ComposeScreen)
                }
            })
        }
    }
}

@NavigationDestination(LegacyDetail::class)
class LegacyDetailFragment : Fragment() {

    private val navigation by navigationHandle<LegacyDetail>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(context).apply {
                // Access key properties directly -- no Bundle parsing needed.
                text = "Legacy Detail: ${navigation.key.itemId}"
            })

            addView(Button(context).apply {
                text = "Go Back"
                setOnClickListener {
                    navigation.close()
                }
            })
        }
    }
}

// ============================================================
// Compose Destination (navigable from Fragment)
// ============================================================
// This Compose destination can be opened from both Fragment and Compose contexts.

@Composable
@NavigationDestination(ComposeScreen::class)
fun ComposeScreenDestination() {
    val navigation = navigationHandle<ComposeScreen>()

    Column {
        Text("Compose Screen")
        Text("This was opened from a Fragment!")

        // Navigate back to the Fragment
        androidx.compose.material3.Button(onClick = { navigation.close() }) {
            Text("Go Back to Fragment")
        }

        // Navigate to another Fragment from Compose
        androidx.compose.material3.Button(onClick = {
            navigation.open(LegacyDetail("item-from-compose"))
        }) {
            Text("Open Legacy Detail (Fragment)")
        }
    }
}

// ============================================================
// Mixed Navigation Setup
// ============================================================
// When using Fragment interop, the navigation container can hold both
// Fragment and Compose destinations. The NavigationDisplay handles rendering
// Compose destinations, while Fragment destinations are rendered in their
// own FragmentContainerView.
//
// In the Activity:
//
// class MainActivity : AppCompatActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         setContent {
//             val container = rememberNavigationContainer(
//                 backstack = backstackOf(FragmentHome.asInstance()),
//             )
//             NavigationDisplay(state = container)
//         }
//     }
// }
//
// The enro-compat module provides the bridge between Fragment and Compose
// navigation systems, ensuring both participate in the same backstack,
// share the same interceptors, and support the same result mechanism.
