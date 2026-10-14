package dev.enro.html

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnroHtmlContentTests {

    @Test
    fun pageLifecycleIsResumedUntilTheCompositionIsDisposed() = runHtmlTest {
        var lifecycleOwner: LifecycleOwner? = null
        var viewModel: TrackedViewModel? = null
        setContent {
            EnroHtmlContent {
                lifecycleOwner = LocalLifecycleOwner.current
                viewModel = viewModel { TrackedViewModel() }
            }
        }
        awaitUntil("the content composes") { lifecycleOwner != null }
        assertEquals(Lifecycle.State.RESUMED, lifecycleOwner!!.lifecycle.currentState)

        disposeContent()

        assertEquals(Lifecycle.State.DESTROYED, lifecycleOwner!!.lifecycle.currentState)
        assertTrue(viewModel!!.cleared, "ViewModels in the page's ViewModelStore are cleared")
    }

    class TrackedViewModel : ViewModel() {
        var cleared = false

        override fun onCleared() {
            cleared = true
        }
    }
}
