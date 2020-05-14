package nav.enro.core.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.context.NavigationContext
import nav.enro.core.executors.ExecutorArgs
import nav.enro.core.executors.NavigationExecutor
import nav.enro.core.executors.override.createActivityToActivityOverride
import nav.enro.core.executors.override.createActivityToFragmentOverride
import nav.enro.core.executors.override.createFragmentToFragmentOverride
import nav.enro.core.executors.override.createOverride
import nav.enro.core.navigator.*

interface NavigationComponentBuilderCommand {
    fun execute(builder: NavigationComponentBuilder)
}

class NavigationComponentBuilder {
    @PublishedApi
    internal val navigators: MutableList<NavigatorDefinition<*, *>> = mutableListOf()
    @PublishedApi
    internal val overrides: MutableList<NavigationExecutor<*, *, *>> = mutableListOf()
    @PublishedApi
    internal val plugins: MutableList<EnroPlugin> = mutableListOf()

    inline fun <reified T : NavigationKey, reified A : FragmentActivity> activityNavigator(
        block: ActivityNavigatorBuilder<T, A>.() -> Unit = {}
    ) {
        navigators.add(createActivityNavigator(block))
    }

    inline fun <reified T : NavigationKey, reified A : Fragment> fragmentNavigator(
        block: FragmentNavigatorBuilder<A, T>.() -> Unit = {}
    ) {
        navigators.add(createFragmentNavigator(block))
    }

    inline fun <reified From : FragmentActivity, reified Opens : FragmentActivity> activityToActivityOverride(
        noinline launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
        noinline close: ((context: NavigationContext<out Opens, out NavigationKey>) -> Unit)
    ) {
        overrides.add(createActivityToActivityOverride(launch, close))
    }

    inline fun <reified From : FragmentActivity, reified Opens : Fragment> activityToFragmentOverride(
        noinline launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
        noinline close: (NavigationContext<out Opens, out NavigationKey>) -> Unit
    ) {
        overrides.add(createActivityToFragmentOverride(launch, close))
    }

    inline fun <reified From : Fragment, reified Opens : Fragment> fragmentToFragmentOverride(
        noinline launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
        noinline close: (NavigationContext<out Opens, out NavigationKey>) -> Unit
    ) {
        overrides.add(createFragmentToFragmentOverride(launch, close))
    }

    inline fun <reified From : Any, reified Opens : Any> override(
        noinline launch: ((ExecutorArgs<out From, out Opens, out NavigationKey>) -> Unit),
        noinline close: (NavigationContext<out Opens, out NavigationKey>) -> Unit
    ) {
        overrides.add(createOverride(launch, close))
    }

    fun add(navigator: NavigatorDefinition<*, *>) {
        navigators.add(navigator)
    }

    fun add(override: NavigationExecutor<*, *, *>) {
        overrides.add(override)
    }

    fun withComponent(builder: NavigationComponentBuilder) {
        navigators.addAll(builder.navigators)
        overrides.addAll(builder.overrides)
        plugins.addAll(builder.plugins)
    }

    fun withPlugin(enroPlugin: EnroPlugin) {
        plugins.add(enroPlugin)
    }

    internal fun build() = NavigationController(navigators, overrides, plugins)
}

/**
 * Create a NavigationController from the NavigationControllerDefinition/DSL, and immediately attach it
 * to the NavigationApplication from which this function was called.
 */
fun NavigationApplication.navigationController(block: NavigationComponentBuilder.() -> Unit = {}): NavigationController {
    return NavigationComponentBuilder()
        .apply { generatedComponent?.execute(this) }
        .apply(block)
        .build()
        .apply {
            NavigationController.install(this@navigationController)
        }
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