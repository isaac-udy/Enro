package dev.enro.controller.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.InitializerViewModelFactoryBuilder
import kotlin.reflect.KClass

internal class ViewModelRepository {
    private val builder = InitializerViewModelFactoryBuilder()

    internal fun <T : ViewModel> register(
        clazz: KClass<T>,
        initializer: CreationExtras.() -> T,
    ) {
        builder.addInitializer(clazz, initializer)
    }

    internal fun getFactory(): ViewModelProvider.Factory {
        return builder.build()
    }
}