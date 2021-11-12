    package dev.enro.core.controller

import android.app.Application
import dev.enro.core.*
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.plugins.EnroPlugin

// TODO get rid of this, or give it a better name
interface NavigationComponentBuilderCommand {
    fun execute(builder: NavigationComponentBuilder)
}

class NavigationComponentBuilder {
    @PublishedApi
    internal val navigators: MutableList<Navigator<*, *>> = mutableListOf()
    @PublishedApi
    internal val overrides: MutableList<NavigationExecutor<*, *, *>> = mutableListOf()
    @PublishedApi
    internal val plugins: MutableList<EnroPlugin> = mutableListOf()
    @PublishedApi
    internal val interceptors: MutableList<NavigationInstructionInterceptor> = mutableListOf()

    fun navigator(navigator: Navigator<*, *>) {
        navigators.add(navigator)
    }

    fun override(override: NavigationExecutor<*, *, *>) {
        overrides.add(override)
    }

    inline fun <reified From : Any, reified Opens : Any> override(
        noinline block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
    ) {
        overrides.add(createOverride(From::class, Opens::class, block))
    }

    fun plugin(enroPlugin: EnroPlugin) {
        plugins.add(enroPlugin)
    }

    fun interceptor(interceptor: NavigationInstructionInterceptor) {
        interceptors.add(interceptor)
    }

    fun component(builder: NavigationComponentBuilder) {
        navigators.addAll(builder.navigators)
        overrides.addAll(builder.overrides)
        plugins.addAll(builder.plugins)
        interceptors.addAll(builder.interceptors)
    }

    internal fun build(): NavigationController {
        return NavigationController().apply {
            addComponent(this@NavigationComponentBuilder)
        }
    }
}

/**
 * Create a NavigationController from the NavigationControllerDefinition/DSL, and immediately attach it
 * to the NavigationApplication from which this function was called.
 */
fun NavigationApplication.navigationController(block: NavigationComponentBuilder.() -> Unit = {}): NavigationController {
    if(this !is Application)
            throw IllegalArgumentException("A NavigationApplication must extend android.app.Application")

    return NavigationComponentBuilder()
        .apply { generatedComponent?.execute(this) }
        .apply(block)
        .build()
        .apply { install(this@navigationController) }
}

private val NavigationApplication.generatedComponent get(): NavigationComponentBuilderCommand? =
    runCatching {
        Class.forName(this::class.java.name + "Navigation")
            .newInstance() as NavigationComponentBuilderCommand
    }.getOrNull()

/**
 * Create a NavigationControllerBuilder, without attaching it to a NavigationApplication.
 *
 * This method is primarily used for composing several builder definitions together in a final NavigationControllerBuilder.
 */
fun createNavigationComponent(block: NavigationComponentBuilder.() -> Unit): NavigationComponentBuilder {
    return NavigationComponentBuilder()
        .apply(block)
}