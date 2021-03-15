package dev.enro.core.result

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import dev.enro.core.*
import dev.enro.core.result.internal.ResultChannelImpl

internal val forwardingResultExecutor = createOverride<Any, Any> {
    closed {
        if(it !is FragmentContext) {
            defaultClosed(it)
            return@closed
        }

        val forwardingData = ResultChannelImpl.getForwardingData(it.getNavigationHandleViewModel())
        val lastActivityIndex = forwardingData.indexOfLast { FragmentActivity::class.java.isAssignableFrom(it.from) }
        if(lastActivityIndex > 0) {
            defaultClosed(it.activity.navigationContext)
            return@closed
        }

        val fragments = forwardingData.mapNotNull { info ->
            it.fragment.parentFragmentManager.findFragmentByTag(info.fromId)
        }

        if(fragments.size != forwardingData.size) {
            defaultClosed(it)
            return@closed
        }

        it.fragment.parentFragmentManager.commit {
            val animations = animationsFor(it, NavigationInstruction.Close)
            setCustomAnimations(animations.enter, animations.exit)

            fragments.drop(1).forEach { fragment ->
                remove(fragment)
            }
            remove(it.fragment)
            attach(fragments.first())
        }
    }
}