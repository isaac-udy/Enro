package dev.enro.result.flow

import dev.enro.NavigationHandle
import dev.enro.annotations.AdvancedEnroApi

/**
 * The [NavigationFlow] that owns this destination's entry, if the
 * destination was opened as part of one — or `null` if the destination
 * is running outside of any flow.
 *
 * Advanced surface: most flow consumers should reach for the typed
 * accessors on `NavigationFlowScope` instead. Use this only when you
 * need to introspect the flow metadata from outside the flow's own
 * scope (e.g. a custom interceptor / plugin).
 */
@AdvancedEnroApi
public val NavigationHandle<*>.navigationFlow: NavigationFlow<*>?
    get() {
        return instance.metadata.get(NavigationFlow.Companion.ResultFlowKey)
    }