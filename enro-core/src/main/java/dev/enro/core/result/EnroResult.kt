package dev.enro.core.result

import dev.enro.core.NavigationHandle
import dev.enro.core.close
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.core.result.internal.ResultChannelImpl

internal class EnroResult: EnroPlugin() {
    private val channels = mutableMapOf<ResultChannelId, ResultChannelImpl<*>>()
    private val pendingResults = mutableMapOf<ResultChannelId, PendingResult>()

    override fun onAttached(navigationController: NavigationController) {
        controllerBindings[navigationController] = this
    }

    override fun onActive(navigationHandle: NavigationHandle) {
        if(ResultChannelImpl.isForwardingResult(navigationHandle)) {
            navigationHandle.close()
            return
        }
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

    internal fun hasPendingResult(resultChannelId: ResultChannelId): Boolean {
        return pendingResults.containsKey(resultChannelId)
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

        @JvmStatic
        fun from(navigationController: NavigationController): EnroResult {
            return controllerBindings[navigationController]
                ?: throw IllegalStateException("Nice Error")
        }
    }
}