// Snippets for docs/ghpages/docs/advanced/dependency-injection.md
package dev.enro.docs.advanced.dependencyinjection

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.viewmodel.createEnroViewModel

class MyViewModel : ViewModel()

private object myFramework {
    fun myViewModel() = MyViewModel()
}

@Composable
fun OtherFrameworks() {
    // region docs:di/other-frameworks
    val viewModel = viewModel<MyViewModel> {
        // Ask your framework for the instance here.
        createEnroViewModel { myFramework.myViewModel() }
    }
    // endregion
}
