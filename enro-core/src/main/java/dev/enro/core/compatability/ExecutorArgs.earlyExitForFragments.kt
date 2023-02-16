package dev.enro.core.compatability

import dev.enro.core.ExecutorArgs
import dev.enro.core.FragmentContext
import dev.enro.core.fragment

internal fun ExecutorArgs<*, *, *>.earlyExitForFragments(): Boolean {
    return fromContext is FragmentContext && !fromContext.fragment.isAdded
}