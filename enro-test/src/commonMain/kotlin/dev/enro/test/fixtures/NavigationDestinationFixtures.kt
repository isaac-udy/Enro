package dev.enro.test.fixtures

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.ui.NavigationDestination
import dev.enro.ui.navigationDestinationContent

private const val TEST_OWNERS = "dev.enro.test.fixtures.NavigationDestinationFixtures.TEST_OWNERS"

public object NavigationDestinationFixtures {
    public fun <T : NavigationKey> create(
        key: T,
        metadata: NavigationDestination.MetadataBuilder<T>.() -> Unit = { },
    ): NavigationDestination<T> {
        return navigationDestinationContent<T>(
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

public val NavigationDestination<*>.lifecycleOwner: LifecycleOwner get() {
    return metadata[TEST_OWNERS] as LifecycleOwner
}

public val NavigationDestination<*>.viewModelStoreOwner: ViewModelStoreOwner get() {
    return metadata[TEST_OWNERS] as ViewModelStoreOwner
}

public val NavigationDestination<*>.defaultViewModelProviderFactory: HasDefaultViewModelProviderFactory get() {
    return metadata[TEST_OWNERS] as HasDefaultViewModelProviderFactory
}