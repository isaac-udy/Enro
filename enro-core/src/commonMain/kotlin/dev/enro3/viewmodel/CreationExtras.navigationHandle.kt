package dev.enro3.viewmodel

import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro3.NavigationHandle
import dev.enro3.NavigationKey

@PublishedApi
internal inline fun <reified K : NavigationKey> CreationExtras.getNavigationHandle(): NavigationHandle<K> {
    val viewModelStoreOwner = requireNotNull(get(VIEW_MODEL_STORE_OWNER_KEY)) {
        "Could not get NavigationHandle from CreationExtras, as the VIEW_MODEL_STORE_OWNER_KEY was not set in the CreationExtras."
    }
    return viewModelStoreOwner.getNavigationHandle()
}
