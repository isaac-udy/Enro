package nav.enro.example.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import nav.enro.core.NavigationHandle
import nav.enro.core.navigationHandle
import java.lang.IllegalStateException
import java.lang.reflect.Constructor

private sealed class FragmentOrActivity() {
    class IsFragment(val fragment: Fragment) : FragmentOrActivity()
    class IsActivity(val activity: FragmentActivity) : FragmentOrActivity()
}

class NavigationViewModelFactory private constructor(
    fragmentOrActivity: FragmentOrActivity,
    defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(
    when (fragmentOrActivity) {
        is FragmentOrActivity.IsActivity -> fragmentOrActivity.activity
        is FragmentOrActivity.IsFragment -> fragmentOrActivity.fragment
    },
    defaultArgs
) {
    private val SIGNATURE_ONE = arrayOf<Class<*>>(
        SavedStateHandle::class.java,
        NavigationHandle::class.java
    )

    private val SIGNATURE_TWO = arrayOf<Class<*>>(
        NavigationHandle::class.java,
        SavedStateHandle::class.java
    )

    private val SIGNATURE_THREE = arrayOf<Class<*>>(
        SavedStateHandle::class.java
    )

    private val SIGNATURE_FOUR = arrayOf<Class<*>>(
        NavigationHandle::class.java
    )

    private val navigationHandle by when (fragmentOrActivity) {
        is FragmentOrActivity.IsActivity -> fragmentOrActivity.activity.navigationHandle<Nothing>()
        is FragmentOrActivity.IsFragment -> fragmentOrActivity.fragment.navigationHandle<Nothing>()
    }

    constructor(
        fragment: Fragment
    ) : this(FragmentOrActivity.IsFragment(fragment), null)

    constructor(
        activity: FragmentActivity
    ) : this(FragmentOrActivity.IsActivity(activity), null)

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val (constructor, signature) = findMatchingConstructor(modelClass) ?: throw IllegalStateException()

        return when {
            SIGNATURE_ONE.contentEquals(signature) -> constructor.newInstance(handle, navigationHandle)
            SIGNATURE_TWO.contentEquals(signature) -> constructor.newInstance(navigationHandle, handle)
            SIGNATURE_THREE.contentEquals(signature) -> constructor.newInstance(handle)
            SIGNATURE_FOUR.contentEquals(signature) -> constructor.newInstance(navigationHandle)
            else -> constructor.newInstance()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> findMatchingConstructor(
        modelClass: Class<T>
    ): Pair<Constructor<T>, Array<Class<*>>>? {
        for (constructor in modelClass.constructors) {
            val parameterTypes = constructor.parameterTypes
            when {
                SIGNATURE_ONE.contentEquals(parameterTypes) -> return (constructor as Constructor<T>) to SIGNATURE_ONE
                SIGNATURE_TWO.contentEquals(parameterTypes) -> return (constructor as Constructor<T>) to SIGNATURE_TWO
                SIGNATURE_THREE.contentEquals(parameterTypes) -> return (constructor as Constructor<T>) to SIGNATURE_THREE
                SIGNATURE_FOUR.contentEquals(parameterTypes) -> return (constructor as Constructor<T>) to SIGNATURE_FOUR
                parameterTypes.isEmpty() -> return (constructor as Constructor<T>) to SIGNATURE_FOUR
            }
        }
        return null
    }
}

abstract class SingleStateViewModel<T : Any>(): ViewModel() {
    private val internalState = MutableLiveData<T>()
    val observableState = internalState as LiveData<T>
    var state get() = internalState.value!!
        protected set(value) {
            internalState.value = value
        }
}