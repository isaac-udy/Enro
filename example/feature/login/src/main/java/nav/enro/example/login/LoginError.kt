package nav.enro.example.login

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.activity
import nav.enro.core.getNavigationHandle
import nav.enro.core.navigationHandle
import nav.enro.core.navigator.SyntheticDestination
import nav.enro.example.core.navigation.LoginErrorKey

class LoginErrorFragment : DialogFragment() {

    private val navigation by navigationHandle<LoginErrorKey>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val key = navigation.key
        return AlertDialog.Builder(requireContext())
            .setTitle("Whoops!")
            .setMessage("\"${key.errorUser}\" isn't a valid user!")
            .create()
    }
}

@NavigationDestination(LoginErrorKey::class)
class LoginErrorDestination : SyntheticDestination<LoginErrorKey> {
    override fun process(
        navigationContext: NavigationContext<out Any, out NavigationKey>,
        instruction: NavigationInstruction.Open<LoginErrorKey>
    ) {
        val activity = navigationContext.activity
        AlertDialog.Builder(activity)
            .setTitle("Error!")
            .setMessage("Whoops! It looks like '${instruction.navigationKey.errorUser}' isn't a valid user.\n\nPlease try again.")
            .setNegativeButton("Close") { _, _ -> }
            .show()
    }
}