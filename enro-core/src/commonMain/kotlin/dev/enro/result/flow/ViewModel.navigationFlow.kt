package dev.enro.result.flow

import androidx.lifecycle.ViewModel
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.getNavigationHandle

@AdvancedEnroApi
public val ViewModel.navigationFlow: NavigationFlow<*>?
    get() {
        return getNavigationHandle().navigationFlow
    }