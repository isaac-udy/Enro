package dev.enro.test.fixtures

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.ui.NavigationDestination
import dev.enro.ui.navigationDestination

private const val TEST_OWNERS = "dev.enro.test.fixtures.NavigationDestinationFixtures.TEST_OWNERS"

object NavigationDestinationFixtures {
    fun <T : NavigationKey> create(
        key: T,
        metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = { },
    ): NavigationDestination<T> {
        return navigationDestination<T>(
            metadata = {
                apply(metadata)
                add(TEST_OWNERS to TestLifecycleAndViewModelStoreOwner())
            },
            content = {
                // Test NavigationDestination doesn't have any content
            }
        ).create(key.asInstance())
    }
}

val NavigationDestination<*>.lifecycleOwner: LifecycleOwner get() {
    return metadata[TEST_OWNERS] as LifecycleOwner
}

val NavigationDestination<*>.viewModelStoreOwner: ViewModelStoreOwner get() {
    return metadata[TEST_OWNERS] as ViewModelStoreOwner
}

val NavigationDestination<*>.defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory get() {
    return metadata[TEST_OWNERS] as HasDefaultViewModelProviderFactory
}