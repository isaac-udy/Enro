package dev.enro.core.controller.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.window.BackEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dev.enro.core.ActivityContext
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.container.emptyBackstack
import dev.enro.core.containerManager
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.navigationContext

internal class ActivityLifecycleCallbacksForEnro(
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        if (activity !is ComponentActivity) return

        val navigationContext = ActivityContext(activity)

        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                fragmentLifecycleCallbacks,
                true
            )

            NavigationContainerProperty(
                lifecycleOwner = activity,
                navigationContainerProducer = {
                    FragmentNavigationContainer(
                        containerId = android.R.id.content,
                        parentContext = activity.navigationContext,
                        accept = { false },
                        emptyBehavior = EmptyBehavior.AllowEmpty,
                        interceptor = {},
                        animations = {},
                        initialBackstack = emptyBackstack(),
                    )
                },
                onContainerAttached = {
                    if (activity.containerManager.activeContainer != it) return@NavigationContainerProperty
                    if (savedInstanceState != null) return@NavigationContainerProperty
                    activity.containerManager.setActiveContainer(null)
                }
            )
        }

        onNavigationContextCreated(navigationContext, savedInstanceState)
        activity.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }

            @RequiresApi(34)
            override fun handleOnBackProgressed(backEvent: BackEvent) {
                isEnabled = false
                activity.onBackPressedDispatcher.dispatchOnBackProgressed(backEvent)
                isEnabled = true
                Log.e("Back", "handleOnBackProgressed ${activity.window.decorView.rootView} ${backEvent.swipeEdge} ${backEvent.progress}")
            }

            @RequiresApi(34)
            override fun handleOnBackStarted(backEvent: BackEvent) {
                isEnabled = false
                activity.onBackPressedDispatcher.dispatchOnBackStarted(backEvent)
                isEnabled = true
                Log.e("Back", "handleOnBackStarted ${backEvent.swipeEdge} ${backEvent.progress}")
            }
        })
//        activity.onBackPressedDispatcher.addCallback(activity) {
//            val leafContext = navigationContext.leafContext()
//            if (interceptBackPressForAndroidxNavigation(this, leafContext)) return@addCallback
//            leafContext.getNavigationHandle().requestClose()
//        }
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
        if (activity !is ComponentActivity) return
        onNavigationContextSaved(activity.navigationContext, outState)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
}