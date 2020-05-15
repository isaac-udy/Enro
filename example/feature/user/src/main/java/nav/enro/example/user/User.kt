package nav.enro.example.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.user.*
import nav.enro.annotations.NavigationDestination
import nav.enro.core.*
import nav.enro.example.core.navigation.LoginKey
import nav.enro.example.core.navigation.UserKey

@NavigationDestination(UserKey::class)
class UserFragment : Fragment() {

    private val navigation by getNavigationHandle<UserKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user, container, false).apply {
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subtitle.text = navigation.key.userId
        logOutButton.setOnClickListener {
            navigation.replaceRoot(LoginKey())
        }
    }
}