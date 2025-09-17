package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.setNavigationHandleTag
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import kotlin.reflect.KClass

@PublishedApi
internal class EnroViewModelFactory(
    private val navigationHandle: NavigationHandle,
    private val delegate: ViewModelProvider.Factory
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val mutableCreationExtras = MutableCreationExtras(extras)
        EnroViewModelNavigationHandleProvider.put(modelClass, navigationHandle)
        val viewModel = try {
            if (mutableCreationExtras[ViewModelProvider.NewInstanceFactory.VIEW_MODEL_KEY] == null) {
                mutableCreationExtras[ViewModelProvider.NewInstanceFactory.VIEW_MODEL_KEY] = getDefaultKey(modelClass.kotlin)
            }
            delegate.create(modelClass, mutableCreationExtras) as T
        } catch (ex: RuntimeException) {
            if (ex is EnroException) throw ex
            throw EnroException.CouldNotCreateEnroViewModel(
                "Failed to created ${modelClass.name} using factory ${delegate::class.java.name}.\n",
                ex
            )
        }
        viewModel.setNavigationHandleTag(navigationHandle)
        EnroViewModelNavigationHandleProvider.clear(modelClass)
        return viewModel
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create(modelClass, CreationExtras.Empty)
    }

    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras,
    ): T {
        val mutableCreationExtras = MutableCreationExtras(extras)
        EnroViewModelNavigationHandleProvider.put(modelClass.java, navigationHandle)
        val viewModel = try {
            if (mutableCreationExtras[ViewModelProvider.NewInstanceFactory.VIEW_MODEL_KEY] == null) {
                mutableCreationExtras[ViewModelProvider.NewInstanceFactory.VIEW_MODEL_KEY] = getDefaultKey(modelClass)
            }
            delegate.create(modelClass, mutableCreationExtras) as T
        } catch (ex: RuntimeException) {
            if (ex is EnroException) throw ex
            throw EnroException.CouldNotCreateEnroViewModel(
                "Failed to created ${modelClass.simpleName} using factory ${delegate::class.java.name}.\n",
                ex
            )
        }
        viewModel.setNavigationHandleTag(navigationHandle)
        EnroViewModelNavigationHandleProvider.clear(modelClass.java)
        return viewModel
    }

    companion object {
        /**
         * See [androidx.lifecycle.viewmodel.internal.ViewModelProviders]
         */
        private const val VIEW_MODEL_PROVIDER_DEFAULT_KEY: String =
            "androidx.lifecycle.ViewModelProvider.DefaultKey"

        internal fun <T : ViewModel> getDefaultKey(modelClass: KClass<T>): String {
            val canonicalName = requireNotNull(modelClass.qualifiedName) {
                "Local and anonymous classes can not be ViewModels"
            }
            return "${VIEW_MODEL_PROVIDER_DEFAULT_KEY}:$canonicalName"
        }
    }
}