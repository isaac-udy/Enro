package dev.enro.platform

import androidx.activity.ComponentActivity
import dev.enro.context.RootContext

public val RootContext.activity: ComponentActivity
    get() {
        require(parent is ComponentActivity) {
            "The parent of the RootContext must be a ComponentActivity, but found ${parent::class.simpleName} instead."
        }
        return parent
    }