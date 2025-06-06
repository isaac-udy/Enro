package dev.enro.platform

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.enro.NavigationKey
import dev.enro.context.RootContext

@PublishedApi
internal class ActivityContextHolder : ViewModel() {
    internal var rootContext: RootContext? = null

    @PublishedApi
    internal var navigationHandle: ActivityNavigationHandle<NavigationKey>? = null

    override fun onCleared() {
        rootContext = null
        navigationHandle = null
    }
}

@PublishedApi
internal val ComponentActivity.activityContextHolder: ActivityContextHolder get() {
    return ViewModelProvider(this).get(ActivityContextHolder::class.java)
}
