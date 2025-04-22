package dev.enro.destination.fragment

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.core.view.doOnAttach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.findFragment
import dev.enro.core.NavigationHost
import dev.enro.core.R
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.plugins.EnroPlugin
import java.util.WeakHashMap

/**
 * This object provides hooks for supporting shared element transitions in Fragments.
 */
public object FragmentSharedElements {
    internal class SharedElement(val view: View, val name: String)
    internal class SharedElementContainer(val map: WeakHashMap<View, String> = WeakHashMap())

    internal fun getSharedElements(fragment: Fragment): List<SharedElement> {
        val container = fragment.view?.getTag(R.id.enro_internal_shared_element_container_id) as? SharedElementContainer
            ?: return emptyList()

        return container.map.map { (view, name) -> SharedElement(view, name) }
    }

    /**
     * This method configures a shared element transition for the View/name combination that is provided. When the Fragment
     * associated with the View is part of a Fragment transaction, the View provided will be added to the transaction
     * using [FragmentTransaction.addSharedElement].
     *
     * If you add a shared element with a name that has already been used, it will cause the View associated with that name to
     * be removed as a shared element.
     *
     * If you've previously configured a shared element transition for a View, but you want to remove it, use [clearSharedElement]
     */
    public fun addSharedElement(view: View, name: String) {
        view.doOnAttach {
            val rootFragmentView = runCatching { view.findFragment<Fragment>() }
                .getOrNull()
                ?.view

            if (rootFragmentView == null) {
                throw IllegalStateException("Cannot add shared element to a View that is not attached to a Fragment")
            }

            val sharedElementContainer =
                rootFragmentView.getTag(R.id.enro_internal_shared_element_container_id) as? SharedElementContainer
                    ?: SharedElementContainer().apply {
                        rootFragmentView.setTag(
                            R.id.enro_internal_shared_element_container_id,
                            this
                        )
                    }

            // ensure we don't have duplicate names
            sharedElementContainer.map.toList().forEach { (otherView, otherName) ->
                if (otherName == name) { sharedElementContainer.map.remove(otherView) }
            }
            sharedElementContainer.map[view] = name
        }
    }

    /**
     * Removes a shared element from the shared element transition for the Fragment that contains the provided View.
     */
    public fun clearSharedElement(view: View) {
        val rootFragmentView = runCatching { view.findFragment<Fragment>() }
            .getOrNull()
            ?.view

        if (rootFragmentView == null) {
            throw IllegalStateException("Cannot clear shared element from a View that is not attached to a Fragment")
        }

        val sharedElementContainer =
            rootFragmentView.getTag(R.id.enro_internal_shared_element_container_id) as? SharedElementContainer
                ?: SharedElementContainer().apply {
                    rootFragmentView.setTag(
                        R.id.enro_internal_shared_element_container_id,
                        this
                    )
                }

        sharedElementContainer.map.remove(view)
    }

    private val delayedTransitionFragments = WeakHashMap<Fragment, Unit>()

    /**
     * This plugin is used to provide interoperability support for Compose and Fragment shared element transitions. You should
     * install this plugin in your NavigationController if you want to enable shared element transitions for Composables that
     * are hosted in FragmentNavigationContainers.
     */
    public val composeCompatibilityPlugin: EnroPlugin = object : EnroPlugin() {
        private val fragmentCallbacks = object : FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f !is NavigationHost) return
                if (v !is ComposeView) return
                f.postponeEnterTransition()
                v.post {
                    if (delayedTransitionFragments.containsKey(f)) return@post
                    f.startPostponedEnterTransition()
                }
            }
        }

        private val activityCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is FragmentActivity) {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallbacks, true)
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }

        override fun onAttached(navigationController: NavigationController) {
            super.onAttached(navigationController)
            navigationController.application.registerActivityLifecycleCallbacks(activityCallbacks)
        }

        override fun onDetached(navigationController: NavigationController) {
            super.onDetached(navigationController)
            navigationController.application.unregisterActivityLifecycleCallbacks(activityCallbacks)
        }
    }

    /**
     * This method is used to configure the shared element transitions for a Composable destination that is hosted in a
     * FragmentNavigationContainer.
     *
     * By default, this method will use android.R.transition.move for shared element transitions, but by providing a value
     * to [sharedElementEnter] or [sharedElementReturn], you can customize the shared element transitions for the Composable.
     * These lambdas expect an [Any?] because that's the same type used by a Fragment's sharedElementEnterTransition and
     * sharedElementReturnTransition.
     *
     * If you need to delay the start of the shared element transition, you can use [rememberDelayedTransitionController] to
     * create a [DelayedTransitionController] that can be used to control the start of the shared element transition.
     */
    @Composable
    public fun ConfigureComposable(
        sharedElementEnter: (Context) -> Any? = { TransitionInflater.from(it).inflateTransition(android.R.transition.move) },
        sharedElementReturn: (Context) -> Any? = { TransitionInflater.from(it).inflateTransition(android.R.transition.move) },
    ) {
        val view = LocalView.current
        LaunchedEffect(view) {
            val fragment = runCatching {
                view.findFragment<Fragment>()
            }.getOrNull()

            if (fragment == null) {
                Log.e("Enro", "Attempted to use FragmentSharedElements.ConfigureComposable in a Composable that is not hosted in a Fragment")
                return@LaunchedEffect
            }
            fragment.sharedElementEnterTransition = sharedElementEnter(fragment.requireContext())
            fragment.sharedElementReturnTransition = sharedElementEnter(fragment.requireContext())
        }
    }

    /**
     * This interface is used to control the start of a delayed shared element transition.
     *
     * When using the FragmentSharedElement interoperability support for Compose, if you need to delay the start of the
     * shared element transition, you can call [FragmentSharedElements.rememberDelayedTransitionController], to get an instance
     * of [DelayedTransitionController]. This will cause the shared element transition to be delayed until you call [start] on
     * the [DelayedTransitionController] instance.
     */
    public fun interface DelayedTransitionController { public fun start() }

    /**
     * [rememberDelayedTransitionController] is used to create a [DelayedTransitionController] that can be used to control the
     * start of a delayed shared element transition when using the FragmentSharedElement interoperability support for Compose.
     * This method should only be called from a Composable that has already called [FragmentSharedElements.ConfigureComposable].
     *
     * @return A [DelayedTransitionController] instance that can be used to control the start of a delayed shared element transition.
     */
    @Composable
    public fun rememberDelayedTransitionController(): DelayedTransitionController {
        val view = LocalView.current
        return remember(view) {
            val fragment = runCatching {
                view.findFragment<Fragment>()
            }.getOrNull()

            if (fragment == null) {
                Log.e("Enro", "Attempted to use FragmentSharedElements.rememberDelayedTransitionController in a Composable that is not hosted in a Fragment")
                return@remember DelayedTransitionController {}
            }
            delayedTransitionFragments[fragment] = Unit
            DelayedTransitionController {
                delayedTransitionFragments.remove(fragment)
                fragment.startPostponedEnterTransition()
            }
        }
    }
}
