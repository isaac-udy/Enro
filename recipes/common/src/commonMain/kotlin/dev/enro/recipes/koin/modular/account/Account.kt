// The account feature of the Koin Modular recipe. See KoinModularApp.kt.
package dev.enro.recipes.koin.modular.account

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
import androidx.lifecycle.ViewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.recipes.koin.koinEnroViewModel
import dev.enro.recipes.koin.modular.KoinAccount
import dev.enro.recipes.koin.modular.KoinSession
import org.koin.dsl.module

class AccountViewModel(
    private val session: KoinSession,
) : ViewModel() {
    private val navigation by navigationHandle<KoinAccount>()

    val userName: String get() = session.userName

    // Writes to the session the orders feature reads, then goes back.
    fun onSignInClicked(name: String) {
        session.userName = name
        navigation.close()
    }
}

val accountModule = module {
    factory { AccountViewModel(session = get()) }
}

@Composable
@NavigationDestination(KoinAccount::class)
fun KoinAccountDestination() {
    val viewModel = koinEnroViewModel<AccountViewModel>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Account: ${viewModel.userName}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { viewModel.onSignInClicked("Ada") }) { Text("Sign in as Ada") }
        Button(onClick = { viewModel.onSignInClicked("Alan") }) { Text("Sign in as Alan") }
    }
}
