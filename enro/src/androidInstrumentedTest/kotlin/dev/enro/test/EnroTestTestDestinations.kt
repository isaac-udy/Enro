package dev.enro.test

import androidx.lifecycle.ViewModel
import dev.enro.TestActivity
import dev.enro.TestFragment
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import dev.enro.core.result.registerForNavigationResult
import dev.enro.viewmodel.enroViewModels
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

interface EnroTestTestKey : NavigationKey {
    val name: String
}

@Parcelize
data class EnroTestTestActivityKey(
    override val name: String = "Activity"
) : EnroTestTestKey

@NavigationDestination(EnroTestTestActivityKey::class)
class EnroTestTestActivity : TestActivity() {
    var result: String? = null

    val navigation by navigationHandle<EnroTestTestActivityKey> {
        defaultKey(EnroTestTestActivityKey())
    }
    val resultChannel by registerForNavigationResult<String> {
        result = it
    }
    val viewModel by enroViewModels<EnroTestViewModel>()
}

@Parcelize
data class EnroTestTestFragmentKey(
    override val name: String = "Fragment"
) : EnroTestTestKey

@NavigationDestination(EnroTestTestFragmentKey::class)
class EnroTestTestFragment : TestFragment() {
    var result: String? = null

    val navigation by navigationHandle<EnroTestTestFragmentKey> {
        defaultKey(EnroTestTestFragmentKey())
    }
    val resultChannel by registerForNavigationResult<String> {
        result = it
    }
    val viewModel by enroViewModels<EnroTestViewModel>()
}

class EnroTestViewModel : ViewModel() {
    val navigationHandle by navigationHandle<EnroTestTestKey>()
}