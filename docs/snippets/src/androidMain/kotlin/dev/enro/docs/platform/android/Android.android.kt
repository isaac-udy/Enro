// Snippets for docs/ghpages/docs/platform/android.md
package dev.enro.docs.platform.android

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.navigationHandle
import kotlinx.serialization.Serializable

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class EditProfile(val userId: String) : NavigationKey

// Stands in for the app's generated resources.
private object R {
    object layout {
        const val fragment_profile = 0
    }

    object id {
        const val title = 0
    }
}

// region docs:android/install
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule { /* optional config */ }
)

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyComponent.installNavigationController(this)
    }
}
// endregion

// region docs:android/fragment
@NavigationDestination(ShowProfile::class)
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val navigation by navigationHandle<ShowProfile>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.title).text =
            "Profile for ${navigation.key.userId}"
    }
}
// endregion

// region docs:android/activity
@NavigationDestination(EditProfile::class)
class EditProfileActivity : AppCompatActivity() {
    private val navigation by navigationHandle<EditProfile>()
    // ...
}
// endregion
