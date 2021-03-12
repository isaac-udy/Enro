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
import kotlinx.android.parcel.Parcelize
import dev.enro.core.NavigationKey
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
    private val containers: Array<out MultistackContainer>,
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
        fragment.containers = containers
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

class MultistackControllerBuilder @PublishedApi internal constructor(){

    private val containers = mutableListOf<MultistackContainer>()

    @AnimRes private var openStackAnimation: Int? = null

    fun <T: NavigationKey> container(@IdRes containerId: Int, rootKey: T) {
        containers.add(MultistackContainer(containerId, rootKey))
    }

    fun openStackAnimation(@AnimRes animationRes: Int) {
        openStackAnimation = animationRes
    }

    internal fun build(
        lifecycleOwner: LifecycleOwner,
        fragmentManager: () -> FragmentManager
    ) = MultistackControllerProperty(
        containers = containers.toTypedArray(),
        openStackAnimation = openStackAnimation,
        lifecycleOwner = lifecycleOwner,
        fragmentManager = fragmentManager
    )
}

fun FragmentActivity.multistackController(
    block: MultistackControllerBuilder.() -> Unit
) = MultistackControllerBuilder().apply(block).build(
    lifecycleOwner = this,
    fragmentManager = { supportFragmentManager }
)