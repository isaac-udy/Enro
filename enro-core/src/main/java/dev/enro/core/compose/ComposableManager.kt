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
import java.lang.IllegalStateException

class EnroComposableManager : ViewModel() {
    val containers: MutableSet<EnroContainerController> = mutableSetOf()

    var activeContainer: EnroContainerController? = null
        private set

    internal fun setActiveContainerById(id: String?) {
        activeContainer = containers.firstOrNull { it.id == id }
    }

    fun setActiveContainer(containerController: EnroContainerController?) {
        if(containerController == null) {
            activeContainer = null
            return
        }
        val selectedContainer = containers.firstOrNull { it.id == containerController.id }
            ?: throw IllegalStateException("EnroContainerController with id ${containerController.id} is not registered with this EnroComposableManager")
        activeContainer = selectedContainer
    }

    @Composable
    internal fun registerState(controller: EnroContainerController): Boolean {
        DisposableEffect(controller) {
            containers += controller
            if(activeContainer == null) {
                activeContainer = controller
            }
            onDispose {
                containers -= controller
                if(activeContainer == controller) {
                    activeContainer = null
                }
            }
        }
        rememberSaveable(controller, saver = object : Saver<Unit, Boolean> {
            override fun restore(value: Boolean) {
                if(value) {
                    activeContainer = controller
                }
                return
            }

            override fun SaverScope.save(value: Unit): Boolean {
                return (activeContainer?.id == controller.id)
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
    val primary = childComposableManager.activeContainer
    if(primary?.accept?.invoke(key) == true) return ComposableHost(primary)

    val secondary = childComposableManager.containers.firstOrNull {
        it.accept(key)
    }

    return secondary?.let {  ComposableHost(it) }
        ?: parentContext()?.composeHostFor(key)
}