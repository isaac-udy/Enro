package dev.enro.example.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.navigationHandle
import dev.enro.core.replaceRoot
import dev.enro.example.core.navigation.LoginKey
import dev.enro.example.core.navigation.UserKey
import dev.enro.example.user.databinding.UserBinding

@NavigationDestination(UserKey::class)
class UserFragment : Fragment() {

    private val navigation by navigationHandle<UserKey>()

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
        UserBinding.bind(view).apply {
            subtitle.text = navigation.key.userId
            logOutButton.setOnClickListener {
                navigation.replaceRoot(LoginKey())
            }
        }
    }
}