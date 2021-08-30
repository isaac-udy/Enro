package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.parentContext

class EnroComposableManager : ViewModel() {
    val containers: MutableSet<EnroContainerController> = mutableSetOf()

    var primaryContainer: EnroContainerController? = null
        private set

    internal fun setPrimaryContainer(id: String?) {
        primaryContainer = containers.firstOrNull { it.id == id }
    }

    @Composable
    internal fun registerState(controller: EnroContainerController): Boolean {
        DisposableEffect(controller) {
            containers += controller
            if(primaryContainer == null) {
                primaryContainer = controller
            }
            onDispose {
                containers -= controller
                if(primaryContainer == controller) {
                    primaryContainer = null
                }
            }
        }
        rememberSaveable(controller, saver = object : Saver<Unit, Boolean> {
            override fun restore(value: Boolean) {
                if(value) {
                    primaryContainer = controller
                }
                return
            }

            override fun SaverScope.save(value: Unit): Boolean {
                return (primaryContainer?.id == controller.id)
            }
        }) {}
        return true
    }
}

val localComposableManager @Composable get() = LocalViewModelStoreOwner.current!!.composableManger

internal val ViewModelStoreOwner.composableManger: EnroComposableManager get() {
    return ViewModelLazy(
        viewModelClass = EnroComposableManager::class,
        storeProducer = { viewModelStore },
        factoryProducer = { ViewModelProvider.NewInstanceFactory() }
    ).value
}

internal class ComposableHost(
    internal val containerController: EnroContainerController
)

internal fun NavigationContext<*>.composeHostFor(key: NavigationKey): ComposableHost? {
    val primary = childComposableManager.primaryContainer
    if(primary?.accept?.invoke(key) == true) return ComposableHost(primary)

    val secondary = childComposableManager.containers.firstOrNull {
        it.accept(key)
    }

    return secondary?.let {  ComposableHost(it) }
        ?: parentContext()?.composeHostFor(key)
}