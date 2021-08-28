package dev.enro.multistack

import android.os.Parcelable
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.ComposableNavigator
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.fragment.FragmentNavigator
import kotlinx.parcelize.Parcelize
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Parcelize
data class MultistackContainer @PublishedApi internal constructor(
    val containerId: Int,
    val rootKey: NavigationKey
) : Parcelable

class MultistackController internal constructor(
    private val multistackController: MultistackControllerFragment
) {

    val activeContainer = multistackController.containerLiveData as LiveData<Int>

    fun openStack(container: MultistackContainer) {
        multistackController.openStack(container)
    }

    fun openStack(container: Int) {
        multistackController.openStack(multistackController.containers.first { it.containerId == container })
    }
}

class MultistackControllerProperty @PublishedApi internal constructor(
    private val containerBuilders: List<()-> MultistackContainer>,
    @AnimRes private val openStackAnimation: Int?,
    private val lifecycleOwner: LifecycleOwner,
    private val fragmentManager: () -> FragmentManager
) : ReadOnlyProperty<Any, MultistackController> {

    val controller: MultistackController by lazy {
        val fragment = fragmentManager().findFragmentByTag(MULTISTACK_CONTROLLER_TAG)
            ?: run {
                val fragment = MultistackControllerFragment()

                fragmentManager()
                    .beginTransaction()
                    .add(fragment, MULTISTACK_CONTROLLER_TAG)
                    .commit()

                return@run fragment
            }

        fragment as MultistackControllerFragment
        fragment.containers = containerBuilders.map { it() }.toTypedArray()
        fragment.openStackAnimation = openStackAnimation

        return@lazy MultistackController(fragment)
    }

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    controller.hashCode()
                }
            }
        })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): MultistackController {
        return controller
    }
}

class MultistackControllerBuilder @PublishedApi internal constructor(
    private val navigationController: () -> NavigationController
){

    private val containerBuilders = mutableListOf<() -> MultistackContainer>()

    @AnimRes private var openStackAnimation: Int? = null

    fun <T: NavigationKey> container(@IdRes containerId: Int, rootKey: T) {
        containerBuilders.add {
            val navigator = navigationController().navigatorForKeyType(rootKey::class)
            val actualKey = when(navigator) {
                is FragmentNavigator -> rootKey
                is ComposableNavigator -> {
                    Class.forName("dev.enro.core.compose.ComposeFragmentHostKey")
                        .getConstructor(
                            NavigationInstruction.Open::class.java,
                            Integer::class.java
                        )
                        .newInstance(
                            NavigationInstruction.Forward(rootKey),
                            containerId
                        ) as NavigationKey
                }
                else -> throw IllegalStateException("TODO")
            }
            MultistackContainer(containerId, actualKey)
        }
    }

    fun openStackAnimation(@AnimRes animationRes: Int) {
        openStackAnimation = animationRes
    }

    internal fun build(
        lifecycleOwner: LifecycleOwner,
        fragmentManager: () -> FragmentManager
    ) = MultistackControllerProperty(
        containerBuilders = containerBuilders,
        openStackAnimation = openStackAnimation,
        lifecycleOwner = lifecycleOwner,
        fragmentManager = fragmentManager
    )
}

fun FragmentActivity.multistackController(
    block: MultistackControllerBuilder.() -> Unit
) = MultistackControllerBuilder { application.navigationController }.apply(block).build(
    lifecycleOwner = this,
    fragmentManager = { supportFragmentManager }
)