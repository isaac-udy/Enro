package dev.enro.result.flow

import dev.enro.NavigationHandle
import dev.enro.annotations.AdvancedEnroApi

@AdvancedEnroApi
public val NavigationHandle<*>.navigationFlow: NavigationFlow<*>?
    get() {
        return instance.metadata.get(NavigationFlow.Companion.ResultFlowKey)
    }