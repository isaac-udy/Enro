package dev.enro.android

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.destination.compose.ComposableDestination
import dev.enro.destination.compose.destination.activity

public val NavigationContext<*>.activity: ComponentActivity
    get() = when (contextReference) {
        is ComponentActivity -> contextReference
        is Fragment -> contextReference.requireActivity()
        is ComposableDestination -> contextReference.owner.activity
        else -> throw EnroException.UnreachableState()
    }