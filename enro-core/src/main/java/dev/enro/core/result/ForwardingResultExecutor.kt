package dev.enro.core.result

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import dev.enro.core.*
import dev.enro.core.result.internal.ResultChannelImpl

internal val forwardingResultExecutor = createOverride<Any, Any> {
    closed {
        if(it.contextReference is Activity) {
            it.activity.finish()
            return@closed
        }

        val forwardingData = ResultChannelImpl.getForwardingData(it.getNavigationHandleViewModel())
        val lastActivityIndex = forwardingData.indexOfLast { FragmentActivity::class.java.isAssignableFrom(it.from) }
        if(lastActivityIndex > 0) {
            it.activity.finish()
            return@closed
        }

        if(it.contextReference !is Fragment) throw IllegalStateException()

        it.contextReference.parentFragmentManager.commit {
            val animations = animationsFor(it, NavigationInstruction.Close)
            setCustomAnimations(animations.enter, animations.exit)

            forwardingData.drop(1).forEach { info ->
                it.contextReference.parentFragmentManager.findFragmentByTag(info.fromId)?.let { remove(it) }
            }
            remove(it.contextReference)
            attach(it.contextReference.parentFragmentManager.findFragmentByTag(forwardingData.first().fromId)!!)
        }
    }
}