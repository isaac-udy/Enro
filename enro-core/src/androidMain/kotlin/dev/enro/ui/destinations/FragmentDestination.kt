package dev.enro.ui.destinations

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.FragmentState
import androidx.fragment.compose.rememberFragmentState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.platform.EnroLog
import dev.enro.platform.getNavigationKeyInstance
import dev.enro.platform.putNavigationKeyInstance
import dev.enro.result.NavigationResultChannel
import dev.enro.result.NavigationResultScope
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDestinationScope
import dev.enro.ui.navigationDestination
import kotlinx.coroutines.Job
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

public inline fun <reified T : NavigationKey, reified F : Fragment> fragmentDestination(
    metadata: Map<String, Any> = emptyMap(),
    noinline arguments: NavigationDestinationScope<T>.() -> Bundle = { Bundle() },
): NavigationDestinationProvider<T> {
    return fragmentDestination(
        keyType = T::class,
        fragmentType = F::class,
        metadata = metadata,
        arguments = arguments,
    )
}

public fun <T : NavigationKey, F : Fragment> fragmentDestination(
    keyType: KClass<T>,
    fragmentType: KClass<F>,
    metadata: Map<String, Any> = emptyMap(),
    arguments: NavigationDestinationScope<T>.() -> Bundle = { Bundle() },
): NavigationDestinationProvider<T> {
    return navigationDestination(metadata) {
        var fragment: F? by remember {
            mutableStateOf(null)
        }
        if (fragmentType.isSubclassOf(DialogFragment::class)) {
            AndroidDialogFragment(
                clazz = fragmentType.java as Class<DialogFragment>,
                tag = navigation.id,
                fragmentState = rememberFragmentState(),
                arguments = arguments().apply {
                    putNavigationKeyInstance(navigation.instance)
                },
            ) { f ->
                fragment = f as F
            }
        } else {
            AndroidFragment(
                clazz = fragmentType.java,
                modifier = Modifier.fillMaxSize(),
                fragmentState = rememberFragmentState(),
                arguments = arguments().apply {
                    putNavigationKeyInstance(navigation.instance)
                },
            ) { f ->
                fragment = f
            }
        }
        DisposableEffect(fragment) {
            val fragment = fragment
            if (fragment == null) return@DisposableEffect onDispose { }

            val navigation = fragment.fragmentContextHolder.navigationHandle
            @Suppress("UNCHECKED_CAST")
            navigation as FragmentNavigationHandle<T>
            navigation.bind(this@navigationDestination)
            onDispose {
                navigation.unbind()
            }
        }
    }
}

public fun <T : NavigationKey> Fragment.navigationHandle(): ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return ReadOnlyProperty<Fragment, NavigationHandle<T>> { fragment, _ ->
        val holder = fragment.fragmentContextHolder
        val navigation = holder.navigationHandle
        val delegate = navigation.delegate
        if (delegate is FragmentNavigationHandle.NotInitialized<NavigationKey>) {
            fragment.arguments?.getNavigationKeyInstance()?.let {
                delegate.instance = it
                navigation.instance = it
            }
        }
        @Suppress("UNCHECKED_CAST")
        return@ReadOnlyProperty navigation as NavigationHandle<T>
    }
}

@PublishedApi
internal val Fragment.fragmentContextHolder: FragmentContextHolder
    get() {
        return ViewModelProvider
            .create(
                owner = this,
                factory = (this as HasDefaultViewModelProviderFactory).defaultViewModelProviderFactory,
            )
            .get(FragmentContextHolder::class)
    }


public inline fun <reified R : Any> Fragment.registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): ReadOnlyProperty<Fragment, NavigationResultChannel<R>> {
    return registerForNavigationResult(R::class, onClosed, onCompleted)
}

public fun <R : Any> Fragment.registerForNavigationResult(
    @Suppress("unused")
    resultType: KClass<R>,
    onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    onCompleted: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): ReadOnlyProperty<Fragment, NavigationResultChannel<R>> {
    val lazyResultChannel = lazy {
        val id = arguments?.getNavigationKeyInstance()?.id ?: TODO()
        NavigationResultChannel<R>(
            id = NavigationResultChannel.Id(
                ownerId = id,
                resultId = onCompleted::class.java.name,
            ),
            onClosed = onClosed as NavigationResultScope<NavigationKey>.() -> Unit,
            onCompleted = onCompleted as NavigationResultScope<NavigationKey>.(R) -> Unit,
            navigationHandle = fragmentContextHolder.navigationHandle,
        )
    }
    var job: Job? = null
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            job = NavigationResultChannel.observe(resultType, lifecycleScope, lazyResultChannel.value)
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            job?.cancel()
        }
    })
    return ReadOnlyProperty<Fragment, NavigationResultChannel<R>> { _, _ ->
        lazyResultChannel.value
    }
}

@PublishedApi
internal class FragmentContextHolder : ViewModel() {
    @PublishedApi
    internal val navigationHandle: FragmentNavigationHandle<NavigationKey> = FragmentNavigationHandle()
}

@PublishedApi
internal class FragmentNavigationHandle<T : NavigationKey>() : NavigationHandle<T>() {
    internal var delegate: NavigationHandle<T> = NotInitialized()
    override lateinit var instance: NavigationKey.Instance<T>

    @AdvancedEnroApi
    override fun execute(operation: NavigationOperation) {
        delegate.execute(operation)
    }

    override val lifecycle: Lifecycle get() = delegate.lifecycle

    internal fun bind(scope: NavigationDestinationScope<T>) {
        instance = scope.navigation.instance
        delegate = scope.navigation
    }

    internal fun unbind() {
        delegate = NotInitialized()
    }

    internal class NotInitialized<T : NavigationKey>() : NavigationHandle<T>() {
        override lateinit var instance: NavigationKey.Instance<T>

        override val lifecycle: Lifecycle = object : Lifecycle() {
            override val currentState: State = State.INITIALIZED
            override fun addObserver(observer: LifecycleObserver) {}
            override fun removeObserver(observer: LifecycleObserver) {}
        }

        override fun execute(
            operation: NavigationOperation,
        ) {
            EnroLog.warn("NavigationHandle with instance $instance has been not been initialised, but has received an operation which will be ignored")
        }
    }
}


@Composable
internal fun <T : DialogFragment> AndroidDialogFragment(
    clazz: Class<T>,
    tag: String,
    modifier: Modifier = Modifier,
    fragmentState: FragmentState = rememberFragmentState(),
    arguments: Bundle = Bundle.EMPTY,
    onUpdate: (T) -> Unit = { },
) {
    val updateCallback = rememberUpdatedState(onUpdate)
    val view = LocalView.current
    val fragmentManager = remember(view) {
        FragmentManager.findFragmentManager(view)
    }
    val tag = currentCompositeKeyHash.toString()
    val context = LocalContext.current
    DisposableEffect(fragmentManager, clazz, fragmentState) {
        var removeEvenIfStateIsSaved = false
        EnroLog.error("Adding with current state ${fragmentManager.findFragmentByTag(tag)}")
        val fragment = fragmentManager.findFragmentByTag(tag)
            ?: fragmentManager.fragmentFactory
                .instantiate(context.classLoader, clazz.name)
                .apply {
                    this as DialogFragment
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    setInitialSavedState(fragmentState.state.value)
                    setArguments(arguments)
                    val transaction = fragmentManager
                            .beginTransaction()
                            .runOnCommit {
                                EnroLog.error("Committing $tag")
                            }
                            .add(this, tag)

                    if (fragmentManager.isStateSaved) {
                        // If the state is saved when we add the fragment,
                        // we want to remove the Fragment in onDispose
                        // if isStateSaved never becomes true for the lifetime
                        // of this AndroidFragment - we use a LifecycleObserver
                        // on the Fragment as a proxy for that signal
                        removeEvenIfStateIsSaved = true
                        lifecycle.addObserver(
                            object : DefaultLifecycleObserver {
                                override fun onStart(owner: LifecycleOwner) {
                                    removeEvenIfStateIsSaved = false
                                    lifecycle.removeObserver(this)
                                }
                            }
                        )
                        transaction.commitNowAllowingStateLoss()
                    } else {
                        transaction.commitNow()
                    }
                }
        @Suppress("UNCHECKED_CAST") updateCallback.value(fragment as T)
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        onDispose {
            val state = fragmentManager.saveFragmentInstanceState(fragment)
            EnroLog.error("Removing with current state ${fragment}")
            fragmentState.state.value = state
            if (removeEvenIfStateIsSaved) {
                // The Fragment was added when the state was saved and
                // isStateSaved never became true for the lifetime of this
                // AndroidFragment, so we unconditionally remove it here
                fragmentManager.commitNow(allowStateLoss = true) { remove(fragment) }
            } else if (!fragmentManager.isStateSaved) {
                // If the state isn't saved, that means that some state change
                // has removed this Composable from the hierarchy
                fragmentManager.commitNow {
                    remove(fragment)
                }
            }
        }
    }
}