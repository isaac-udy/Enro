package dev.enro.core.synthetic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey

public abstract class SyntheticDestination<T : NavigationKey> {

    private var _navigationContext: NavigationContext<out Any>? = null
    public val navigationContext: NavigationContext<out Any> get() = _navigationContext!!

    public lateinit var key: T
        internal set

    public lateinit var instruction: AnyOpenInstruction
        internal set

    internal fun bind(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    ) {
        this._navigationContext = navigationContext
        @Suppress("UNCHECKED_CAST")
        this.key = instruction.navigationKey as T
        this.instruction = instruction

        navigationContext.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event == Lifecycle.Event.ON_DESTROY) {
                    navigationContext.lifecycle.removeObserver(this)
                    _navigationContext = null
                }
            }
        })
    }

    public abstract fun process()
}