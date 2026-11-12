package dev.enro.result.flow

import dev.enro.NavigationHandle
import dev.enro.annotations.AdvancedEnroApi

/**
 * The [NavigationFlow] that this destination created with `registerForFlowResult`, or `null`
 * if this destination has not created a [NavigationFlow].
 *
 * A destination opened as a step of a flow resolves the flow with [getNavigationFlow].
 */
@AdvancedEnroApi
public val NavigationHandle<*>.navigationFlow: NavigationFlow<*>?
    get() {
        return instance.metadata.get(NavigationFlow.Companion.ResultFlowKey)
    }