package nav.enro.result

import nav.enro.core.NavigationHandle
import nav.enro.core.controller.NavigationController
import nav.enro.core.plugins.EnroPlugin
import nav.enro.result.internal.ResultChannelImpl
import nav.enro.result.internal.ResultChannelId
import nav.enro.result.internal.PendingResult
import java.lang.IllegalStateException

class EnroResult: EnroPlugin() {
    private val channels = mutableMapOf<ResultChannelId, ResultChannelImpl<*>>()
    private val pendingResults = mutableMapOf<ResultChannelId, PendingResult>()

    override fun onAttached(navigationController: NavigationController) {
        controllerBindings[navigationController] = this
    }

    override fun onActive(navigationHandle: NavigationHandle<*>) {
        channels.values
            .filter { channel ->
                pendingResults.any { it.key == channel.id }
            }
            .forEach {
                val result = consumePendingResult(it.id) ?: return@forEach
                it.consumeResult(result.result)
            }
    }

    internal fun addPendingResult(result: PendingResult) {
        val channel = channels[result.resultChannelId]
        if(channel != null) {
            channel.consumeResult(result.result)
        }
        else {
            pendingResults[result.resultChannelId] = result
        }
    }

    private fun consumePendingResult(resultChannelId: ResultChannelId): PendingResult? {
        val result = pendingResults[resultChannelId] ?: return null
        if(resultChannelId.resultId != result.resultChannelId.resultId) return null
        pendingResults.remove(resultChannelId)
        return result
    }

    internal fun registerChannel(channel: ResultChannelImpl<*>) {
        channels[channel.id] = channel
        val result = consumePendingResult(channel.id) ?: return
        channel.consumeResult(result.result)
    }

    internal fun deregisterChannel(channel: ResultChannelImpl<*>) {
        channels.remove(channel.id)
    }

    companion object {
        private val controllerBindings = mutableMapOf<NavigationController, EnroResult>()

        fun from(navigationController: NavigationController): EnroResult {
            return controllerBindings[navigationController]
                ?: throw IllegalStateException("Nice Error")
        }
    }
}