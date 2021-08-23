package dev.enro.core.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.parentContext

class EnroComposableManager : ViewModel() {
    val enroContainers: MutableSet<EnroContainerState> = mutableSetOf()
    var primaryContainer: EnroContainerState? = null
    var parentComposableManager: EnroComposableManager? = null
}

internal val ViewModelStoreOwner.composableManger: EnroComposableManager get() {
    return ViewModelLazy(
        viewModelClass = EnroComposableManager::class,
        storeProducer = { viewModelStore },
        factoryProducer = { ViewModelProvider.NewInstanceFactory() }
    ).value
}

internal class ComposableHost(
    internal val containerState: EnroContainerState
)

internal fun NavigationContext<*>.composeHostFor(key: NavigationKey): ComposableHost? {
    val primary = childComposableManager.primaryContainer
    if(primary?.accept?.invoke(key) == true) return ComposableHost(primary)

    val secondary = childComposableManager.enroContainers.firstOrNull {
        it.accept(key)
    }

    return secondary?.let {  ComposableHost(it) }
        ?: parentContext()?.composeHostFor(key)
}