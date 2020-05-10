package nav.enro.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import nav.enro.core.NavigationHandle
import nav.enro.core.navigationHandle
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.reflect.Constructor
import kotlin.reflect.KClass

private class ConstructorSignature(
    vararg arguments: KClass<*>
) {
    val arguments: Array<Class<*>> = arguments
        .map { it.java }
        .toTypedArray()

    fun <T> bind(
        constructor: Constructor<T>,
        application: Application,
        savedStateHandle: SavedStateHandle,
        navigationHandle: NavigationHandle<*>
    ) : (() -> T)? {
        val parameterTypes = constructor.parameterTypes
        if(!arguments.contentEquals(constructor.parameterTypes)) return null
        val orderedArguments: Array<Any> = parameterTypes.mapNotNull {
            when(it) {
                Application::class.java -> application
                SavedStateHandle::class.java -> savedStateHandle
                NavigationHandle::class.java -> navigationHandle
                else -> null as Any?
            }
        }.toTypedArray()

        return  { constructor.newInstance(*orderedArguments) }
    }
}

private val signatures = listOf(
    ConstructorSignature(
        Application::class
    ),
    ConstructorSignature(
        SavedStateHandle::class
    ),
    ConstructorSignature(
        NavigationHandle::class
    ),
    ConstructorSignature(
        Application::class,
        NavigationHandle::class
    ),
    ConstructorSignature(
        Application::class,
        SavedStateHandle::class
    ),
    ConstructorSignature(
        Application::class,
        SavedStateHandle::class,
        NavigationHandle::class
    )
)

class NavigationViewModelFactory(
    fragmentOrActivity: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractNavigationViewModelFactory(fragmentOrActivity, defaultArgs) {

    private val application by lazy {
        when (fragmentOrActivity) {
            is FragmentActivity -> fragmentOrActivity.application
            is Fragment -> fragmentOrActivity.requireActivity().application
            else -> throw IllegalArgumentException("The 'owner' argument for a NavigationViewModelFactory must be a Fragment activity or a Fragment")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        savedStateHandle: SavedStateHandle,
        navigationHandle: NavigationHandle<*>
    ): T {
        modelClass.constructors.forEach {constructor ->
            signatures.forEach { signature ->
               val constructorBinding = signature.bind(
                    constructor = constructor as Constructor<T>,
                    application = application,
                    savedStateHandle = savedStateHandle,
                    navigationHandle = navigationHandle
               )

               if(constructorBinding != null) {
                   return@create constructorBinding.invoke()
               }
            }
        }
        return modelClass.newInstance()
    }
}