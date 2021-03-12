package dev.enro.example.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.login.*
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.forward
import dev.enro.core.navigationHandle
import dev.enro.core.replaceRoot
import dev.enro.example.core.base.SingleStateViewModel
import dev.enro.example.core.data.UserRepository
import dev.enro.example.core.navigation.DashboardKey
import dev.enro.example.core.navigation.LoginErrorKey
import dev.enro.example.core.navigation.LoginKey
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle

@NavigationDestination(
    key = LoginKey::class
)
class LoginActivity : AppCompatActivity() {

    private val viewModel by enroViewModels<LoginViewModel>()
    private val navigation by navigationHandle<LoginKey> {
        defaultKey(LoginKey())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        viewModel.observableState.observe(this) {
            if(userInput.text.toString() != it.username) {
                userInput.setTextKeepState(it.username)
            }
        }

        userInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onUserNameUpdated(text?.toString() ?: "")
        }

        loginButton.setOnClickListener {
            viewModel.onLogin()
        }
    }
}

data class LoginState(
    val username: String = ""
)

class LoginViewModel : SingleStateViewModel<LoginState>() {

    private val navigationHandle by navigationHandle<NavigationKey>()

    private val userRepo = UserRepository.instance

    init {
        UserRepository.instance.activeUser = null
        state = LoginState()
    }

    fun onUserNameUpdated(username: String) {
        state = state.copy(
            username = username
        )
    }

    fun onLogin() {
        val user = userRepo.getUsers().firstOrNull {
            it.equals(state.username, ignoreCase = true)
        }
        when(user) {
            null -> navigationHandle.forward(
                LoginErrorKey(
                    state.username
                )
            )
            else -> {
                UserRepository.instance.activeUser = user
                navigationHandle.replaceRoot(
                    DashboardKey(user)
                )
            }
        }
    }
}