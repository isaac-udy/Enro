package dev.enro.core

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.enro.core.compose.*
import dev.enro.core.compose.EnroDestinationStorage
import dev.enro.core.fragment.DefaultFragmentExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

sealed class EmptyBehavior {
    /**
     * When this container is about to become empty, allow this container to become empty
     */
    object AllowEmpty : EmptyBehavior()

    /**
     * When this container is about to become empty, do not close the NavigationDestination in the
     * container, but instead close the parent NavigationDestination (i.e. the owner of this container)
     */
    object CloseParent : EmptyBehavior()

    /**
     * When this container is about to become empty, execute an action. If the result of the action function is
     * "true", then the action is considered to have consumed the request to become empty, and the container
     * will not close the last navigation destination. When the action function returns "false", the default
     * behaviour will happen, and the container will become empty.
     */
    class Action(
            val onEmpty: () -> Boolean
    ) : EmptyBehavior()
}

class NavigationContainerManager {
    val containers: MutableSet<NavigationContainer> = mutableSetOf()

    internal val activeContainerState: MutableStateFlow<NavigationContainer?> = MutableStateFlow(null)
    val activeContainer: NavigationContainer? get() = activeContainerState.value

    internal fun setActiveContainerById(id: String?) {
        activeContainerState.value = containers.firstOrNull { it.id == id }
    }

    fun setActiveContainer(containerController: NavigationContainer?) {
        if(containerController == null) {
            activeContainerState.value = null
            return
        }
        val selectedContainer = containers.firstOrNull { it.id == containerController.id }
            ?: throw IllegalStateException("NavigationContainer with id ${containerController.id} is not registered with this NavigationContainerManager")
        activeContainerState.value = selectedContainer
    }

    companion object {
        const val ACTIVE_CONTAINER_KEY = "dev.enro.core.NavigationContainerManager.ACTIVE_CONTAINER_KEY"
    }
}

fun NavigationContainerManager.save(outState: Bundle) {
    containers.forEach { it.save(outState) }
    outState.putString(NavigationContainerManager.ACTIVE_CONTAINER_KEY, activeContainer?.id)
}

fun NavigationContainerManager.restore(savedInstanceState: Bundle?) {
    if(savedInstanceState == null) return
    containers.forEach { it.restore(savedInstanceState) }
    val activeContainer = savedInstanceState.getString(NavigationContainerManager.ACTIVE_CONTAINER_KEY)
    setActiveContainerById(activeContainer)
}

@Composable
internal fun NavigationContainerManager.registerState(controller: ComposableNavigationContainer): Boolean {
    containers += controller
    DisposableEffect(controller) {
        if(activeContainer == null) {
            activeContainerState.value = controller
        }
        onDispose {
            containers -= controller
            if(activeContainer == controller) {
                activeContainerState.value = null
            }
        }
    }
    rememberSaveable(controller, saver = object : Saver<Unit, Boolean> {
        override fun restore(value: Boolean) {
            if(value) {
                activeContainerState.value = controller
            }
            return
        }

        override fun SaverScope.save(value: Unit): Boolean {
            return (activeContainer?.id == controller.id)
        }
    }) {}
    return true
}

interface NavigationContainer {
    val id: String
    val parentContext: NavigationContext<*>
    val backstackFlow: StateFlow<EnroContainerBackstackState>
    val activeContext: NavigationContext<*>?
    val accept: (NavigationKey) -> Boolean
    val emptyBehavior: EmptyBehavior

    fun setBackstack(backstack: EnroContainerBackstackState)

    companion object {
        internal const val BACKSTACK_KEY = "dev.enro.core.INavigationContainer.BACKSTACK_KEY"
    }
}

val NavigationContainer.isActive: Boolean
    get() = parentContext.containerManager.activeContainer == this

fun NavigationContainer.setActive() {
    parentContext.containerManager.setActiveContainer(this)
}

internal fun NavigationContainer.save(outState: Bundle) {
    outState.putParcelableArrayList(
        "${NavigationContainer.BACKSTACK_KEY}@$id", ArrayList(backstackFlow.value.backstackEntries)
    )
}

internal fun NavigationContainer.restore(savedInstanceState: Bundle?) {
    if(savedInstanceState == null) return
    val backstackEntries = savedInstanceState.getParcelableArrayList<EnroContainerBackstackEntry>(
        "${NavigationContainer.BACKSTACK_KEY}@$id"
    )
    val backstack = EnroContainerBackstackState(
        backstackEntries = backstackEntries ?: emptyList(),
        exiting = null,
        exitingIndex = -1,
        lastInstruction = backstackEntries?.lastOrNull()?.instruction ?: NavigationInstruction.Close,
        skipAnimations = true
    )
    setBackstack(backstack)
}

class ComposableNavigationContainer internal constructor(
    override val id: String,
    override val parentContext: NavigationContext<*>,
    override val accept: (NavigationKey) -> Boolean,
    override val emptyBehavior: EmptyBehavior,
    private val destinationStorage: EnroDestinationStorage,
    internal val saveableStateHolder: SaveableStateHolder,
) : NavigationContainer {

    private val mutableBackstack: MutableStateFlow<EnroContainerBackstackState> = MutableStateFlow(createEmptyBackStack())
    override val backstackFlow: StateFlow<EnroContainerBackstackState> get() = mutableBackstack

    private val destinationContexts = destinationStorage.destinations.getOrPut(id) { mutableMapOf() }
    private val currentDestination get() = mutableBackstack.value.backstack
        .mapNotNull { destinationContexts[it.instructionId] }
        .lastOrNull {
            it.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        }

    override val activeContext: NavigationContext<*>?
        get() = currentDestination?.destination?.navigationContext

    override fun setBackstack(backstack: EnroContainerBackstackState) {
        mutableBackstack.value = backstack

        if(backstack.backstack.isEmpty()) {
            parentContext.containerManager.setActiveContainer(null)
            when(emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }
                EmptyBehavior.CloseParent -> {
                    parentContext.getNavigationHandle().close()
                    return
                }
                is EmptyBehavior.Action -> {
                    val consumed = emptyBehavior.onEmpty()
                    if (consumed) {
                        return
                    }
                }
            }
        }
        else {
            parentContext.containerManager.setActiveContainer(this)
        }
    }

    internal fun onInstructionDisposed(instruction: NavigationInstruction.Open) {
        if (mutableBackstack.value.exiting == instruction) {
            mutableBackstack.value = mutableBackstack.value.copy(
                exiting = null,
                exitingIndex = -1
            )
        }
    }

    internal fun getDestinationContext(instruction: NavigationInstruction.Open): ComposableDestinationContextReference {
        val destinationContextReference = destinationContexts.getOrPut(instruction.instructionId) {
            val controller = parentContext.controller
            val composeKey = instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination

            return@getOrPut getComposableDestinationContext(
                instruction = instruction,
                destination = destination,
                parentContainer = this
            )
        }
        destinationContextReference.parentContainer = this@ComposableNavigationContainer
        return destinationContextReference
    }

    @SuppressLint("ComposableNaming")
    @Composable
    internal fun bindDestination(instruction: NavigationInstruction.Open) {
        DisposableEffect(true) {
            onDispose {
                if(!mutableBackstack.value.backstack.contains(instruction)) {
                    destinationContexts.remove(instruction.instructionId)
                }
            }
        }
    }
}

class FragmentNavigationContainer(
    @IdRes val containerId: Int,
    private val parentContextFactory: () -> NavigationContext<*>,
    override val accept: (NavigationKey) -> Boolean,
    override val emptyBehavior: EmptyBehavior,
    internal val fragmentManager: () -> FragmentManager
) : NavigationContainer {
    override val id: String = containerId.toString()

    private val mutableBackstack: MutableStateFlow<EnroContainerBackstackState> = MutableStateFlow(createEmptyBackStack())
    override val backstackFlow: StateFlow<EnroContainerBackstackState> get() = mutableBackstack

    override val parentContext: NavigationContext<*>
        get() = parentContextFactory()

    override val activeContext: NavigationContext<*>?
        get() = fragmentManager().findFragmentById(containerId)?.navigationContext

    override fun setBackstack(backstack: EnroContainerBackstackState) {
        val lastBackstack = backstackFlow.value
        mutableBackstack.value = backstack

        val manager = fragmentManager()
        val toRemoveEntries = lastBackstack.backstackEntries
            .filter {
                !backstack.backstackEntries.contains(it)
            }
        val toRemove = toRemoveEntries
            .mapNotNull {
                manager.findFragmentByTag(it.instruction.instructionId)
            }
        val toDetach = backstack.backstack.dropLast(1)
            .mapNotNull {
                manager.findFragmentByTag(it.instructionId)
            }
        val activeInstruction = backstack.backstack.lastOrNull()
        val activeFragment = activeInstruction?.let {
            manager.findFragmentByTag(it.instructionId)
        }
        val newFragment = if(activeFragment == null && activeInstruction != null) {
            DefaultFragmentExecutor.createFragment(
                manager,
                parentContext.controller.navigatorForKeyType(activeInstruction.navigationKey::class)!!,
                activeInstruction
            )
        } else null

        manager.commit {
            toRemove.forEach {
                remove(it)
            }
            toDetach.forEach {
                detach(it)
            }

            if(activeInstruction == null) return@commit

            if(activeFragment != null) {
                attach(activeFragment)
                setPrimaryNavigationFragment(activeFragment)
            }
            if(newFragment != null) {
                add(containerId, newFragment, activeInstruction.instructionId)
                setPrimaryNavigationFragment(activeFragment)
            }
        }

        if(backstack.lastInstruction is NavigationInstruction.Close) {
            parentContext.containerManager.setActiveContainerById(
                toRemoveEntries.firstOrNull()?.previouslyActiveContainerId
            )
        }
        else {
            parentContext.containerManager.setActiveContainer(this)
        }

        if(backstack.backstack.isEmpty()) {
            when(emptyBehavior) {
                EmptyBehavior.AllowEmpty -> {
                    /* If allow empty, pass through to default behavior */
                }
                EmptyBehavior.CloseParent -> {
                    parentContext.getNavigationHandle().close()
                    return
                }
                is EmptyBehavior.Action -> {
                    val consumed = emptyBehavior.onEmpty()
                    if (consumed) {
                        return
                    }
                }
            }
        }
    }
}

fun FragmentNavigationContainer.isValidContainer(): Boolean {
    val container = when(val parentContextReference = parentContext.contextReference) {
        is Fragment -> parentContextReference.view?.findViewById<View>(containerId)
        is Activity -> parentContextReference.findViewById<View>(containerId)
        else -> null
    }
    return container != null
}

class NavigationContainerProperty @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val root: () -> NavigationKey?,
    private val navigationContainer: FragmentNavigationContainer
) : ReadOnlyProperty<Any, FragmentNavigationContainer> {

    init {
        lifecycleOwner.lifecycleScope.launchWhenCreated {
            val rootKey = root() ?: return@launchWhenCreated
            navigationContainer.setBackstack(
                createEmptyBackStack().push(NavigationInstruction.Replace(rootKey), null)
            )
        }
        pendingContainers.getOrPut(lifecycleOwner.hashCode()) { mutableListOf() }
            .add(WeakReference(navigationContainer))
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): FragmentNavigationContainer {
        return navigationContainer
    }

    companion object {
        private val pendingContainers =
            mutableMapOf<Int, MutableList<WeakReference<FragmentNavigationContainer>>>()

        internal fun getPendingContainers(lifecycleOwner: LifecycleOwner): List<FragmentNavigationContainer> {
            val pending = pendingContainers[lifecycleOwner.hashCode()] ?: return emptyList()
            val containers = pending.mapNotNull { it.get() }
            pendingContainers.remove(lifecycleOwner.hashCode())
            return containers
        }
    }
}

fun FragmentActivity.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty = NavigationContainerProperty(
    this,
    root,
    FragmentNavigationContainer(
        parentContextFactory = { navigationContext },
        containerId = containerId,
        emptyBehavior = emptyBehavior,
        accept = accept,
        fragmentManager = { supportFragmentManager }
    )
)

fun Fragment.navigationContainer(
    @IdRes containerId: Int,
    root: () -> NavigationKey? = { null },
    emptyBehavior: EmptyBehavior = EmptyBehavior.AllowEmpty,
    accept: (NavigationKey) -> Boolean = { true },
): NavigationContainerProperty = NavigationContainerProperty(
    this,
    root,
    FragmentNavigationContainer(
        containerId = containerId,
        parentContextFactory = { navigationContext },
        emptyBehavior = emptyBehavior,
        accept = accept,
        fragmentManager = { childFragmentManager }
    )
)

fun Fragment.composeNavigationContainer() {

}