package dev.enro.core.controller

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import dev.enro.core.*
import dev.enro.core.activity.createActivityNavigationBinding
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.createComposableNavigationBinding
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.controller.repository.ComposeEnvironment
import dev.enro.core.fragment.createFragmentNavigationBinding
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.core.synthetic.createSyntheticNavigationBinding

public interface NavigationComponentBuilderCommand {
    public fun execute(builder: NavigationComponentBuilder)
}

public class NavigationComponentBuilder {
    @PublishedApi
    internal val bindings: MutableList<NavigationBinding<*, *>> = mutableListOf()

    @PublishedApi
    internal val overrides: MutableList<NavigationExecutor<*, *, *>> = mutableListOf()

    @PublishedApi
    internal val plugins: MutableList<EnroPlugin> = mutableListOf()

    @PublishedApi
    internal val interceptors: MutableList<NavigationInstructionInterceptor> = mutableListOf()

    @PublishedApi
    internal var composeEnvironment: ComposeEnvironment? = null

    public fun binding(binding: NavigationBinding<*, *>) {
        bindings.add(binding)
    }

    public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComponentActivity> activityDestination() {
        bindings.add(createActivityNavigationBinding<KeyType, DestinationType>())
    }

    public inline fun <reified KeyType : NavigationKey, reified DestinationType : Fragment> fragmentDestination() {
        bindings.add(createFragmentNavigationBinding<KeyType, DestinationType>())
    }

    public inline fun <reified KeyType : NavigationKey, reified DestinationType : ComposableDestination> composableDestination() {
        bindings.add(createComposableNavigationBinding<KeyType, DestinationType>())
    }

    public inline fun <reified KeyType : NavigationKey> composableDestination(noinline content: @Composable () -> Unit) {
        bindings.add(createComposableNavigationBinding<KeyType>(content))
    }

    public inline fun <reified KeyType : NavigationKey, reified DestinationType : SyntheticDestination<KeyType>> syntheticDestination() {
        bindings.add(createSyntheticNavigationBinding<KeyType, DestinationType>())
    }

    public inline fun <reified KeyType : NavigationKey> syntheticDestination(noinline destination: () -> SyntheticDestination<KeyType>) {
        bindings.add(createSyntheticNavigationBinding(destination))
    }

    public fun override(override: NavigationExecutor<*, *, *>) {
        overrides.add(override)
    }

    public inline fun <reified From : Any, reified Opens : Any> override(
        noinline block: NavigationExecutorBuilder<From, Opens, NavigationKey>.() -> Unit
    ) {
        overrides.add(createOverride(From::class, Opens::class, block))
    }

    public fun plugin(enroPlugin: EnroPlugin) {
        plugins.add(enroPlugin)
    }

    public fun interceptor(interceptor: NavigationInstructionInterceptor) {
        interceptors.add(interceptor)
    }

    public fun composeEnvironment(environment: @Composable (@Composable () -> Unit) -> Unit) {
        composeEnvironment = { content -> environment(content) }
    }

    public fun component(builder: NavigationComponentBuilder) {
        bindings.addAll(builder.bindings)
        overrides.addAll(builder.overrides)
        plugins.addAll(builder.plugins)
        interceptors.addAll(builder.interceptors)

        if (builder.composeEnvironment != null) {
            composeEnvironment = builder.composeEnvironment
        }
    }

    internal fun build(): NavigationController {
        return NavigationController().apply {
            addComponent(this@NavigationComponentBuilder)
        }
    }
}

@Deprecated(
    message = "Please replace with [createNavigationController]",
    replaceWith = ReplaceWith("createNavigationController(strictMode, block)")
)
public fun NavigationApplication.navigationController(
    strictMode: Boolean = false,
    block: NavigationComponentBuilder.() -> Unit = {}
): NavigationController = createNavigationController(strictMode, block)

/**
 * Create a NavigationController from the NavigationControllerDefinition/DSL, and immediately attach it
 * to the NavigationApplication from which this function was called.
 */
public fun NavigationApplication.createNavigationController(
    strictMode: Boolean = false,
    block: NavigationComponentBuilder.() -> Unit = {}
): NavigationController {
    if (this !is Application)
        throw IllegalArgumentException("A NavigationApplication must extend android.app.Application")

    return NavigationComponentBuilder()
        .apply { generatedComponent?.execute(this) }
        .apply(block)
        .build()
        .apply {
            isStrictMode = strictMode
            install(this@createNavigationController)
        }
}

private val NavigationApplication.generatedComponent
    get(): NavigationComponentBuilderCommand? =
        runCatching {
            Class.forName(this::class.java.name + "Navigation")
                .newInstance() as NavigationComponentBuilderCommand
        }.getOrNull()

/**
 * Create a NavigationControllerBuilder, without attaching it to a NavigationApplication.
 *
 * This method is primarily used for composing several builder definitions together in a final NavigationControllerBuilder.
 */
public fun createNavigationComponent(block: NavigationComponentBuilder.() -> Unit): NavigationComponentBuilder {
    return NavigationComponentBuilder()
        .apply(block)
}