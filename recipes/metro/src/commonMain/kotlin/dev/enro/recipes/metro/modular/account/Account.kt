// The account feature of the Metro Modular recipe. See MetroModularApp.kt.
package dev.enro.recipes.metro.modular.account

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
import dev.enro.recipes.metro.modular.MetroAccount
import dev.enro.recipes.metro.modular.MetroSession
import dev.enro.recipes.metro.modular.metroEnroViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject

@Inject
class AccountViewModel(
    private val session: MetroSession,
) : ViewModel() {
    private val navigation by navigationHandle<MetroAccount>()

    val userName: String get() = session.userName

    // Writes to the session the orders feature reads, then goes back.
    fun onSignInClicked(name: String) {
        session.userName = name
        navigation.close()
    }
}

@ContributesTo(AppScope::class)
interface AccountGraph {
    val accountViewModel: AccountViewModel
}

@Composable
@NavigationDestination(MetroAccount::class)
fun MetroAccountDestination() {
    val viewModel = metroEnroViewModel<AccountGraph, AccountViewModel> { it.accountViewModel }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Account: ${viewModel.userName}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { viewModel.onSignInClicked("Ada") }) { Text("Sign in as Ada") }
        Button(onClick = { viewModel.onSignInClicked("Alan") }) { Text("Sign in as Alan") }
    }
}
