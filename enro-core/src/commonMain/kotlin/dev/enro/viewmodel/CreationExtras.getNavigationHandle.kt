package dev.enro.viewmodel

import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import dev.enro.core.getNavigationHandle
import kotlin.jvm.JvmName

public fun CreationExtras.getNavigationHandle(): NavigationHandle {
    val viewModelStoreOwner = requireNotNull(get(VIEW_MODEL_STORE_OWNER_KEY)) {
        "Could not get NavigationHandle from CreationExtras, as the VIEW_MODEL_STORE_OWNER_KEY was not set in the CreationExtras."
    }
    return viewModelStoreOwner.getNavigationHandle()
}

@JvmName("getTypedNavigationHandle")
public inline fun <reified T : NavigationKey> CreationExtras.getNavigationHandle(): TypedNavigationHandle<T> {
    val viewModelStoreOwner = requireNotNull(get(VIEW_MODEL_STORE_OWNER_KEY)) {
        "Could not get NavigationHandle from CreationExtras, as the VIEW_MODEL_STORE_OWNER_KEY was not set in the CreationExtras."
    }
    return viewModelStoreOwner.getNavigationHandle().asTyped()
}