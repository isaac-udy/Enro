package dev.enro.core.result

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationHandle
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.core.result.internal.ResultChannelImpl

@PublishedApi
internal class EnroResult: EnroPlugin() {
    private val channels = mutableMapOf<ResultChannelId, ResultChannelImpl<*, *>>()
    private val pendingResults = mutableMapOf<ResultChannelId, PendingResult>()

    override fun onAttached(navigationController: NavigationController) {
        controllerBindings[navigationController] = this
    }

    override fun onDetached(navigationController: NavigationController) {
        controllerBindings.remove(navigationController)
    }

    override fun onActive(navigationHandle: NavigationHandle) {
        channels.values
            .filter { channel ->
                pendingResults.any { it.key == channel.id }
            }
            .forEach {
                val result = consumePendingResult(it.id) ?: return@forEach
                it.consumeResult(result)
            }
    }

    internal fun addPendingResult(result: PendingResult) {
        val channel = channels[result.resultChannelId]
        if(channel != null) {
            channel.consumeResult(result)
        }
        else {
            pendingResults[result.resultChannelId] = result
        }
    }

    internal fun hasPendingResultFrom(instruction: AnyOpenInstruction): Boolean {
        return pendingResults[instruction.internal.resultId] != null
    }

    private fun consumePendingResult(resultChannelId: ResultChannelId): PendingResult? {
        val result = pendingResults[resultChannelId] ?: return null
        if(resultChannelId.resultId != result.resultChannelId.resultId) return null
        pendingResults.remove(resultChannelId)
        return result
    }

    @PublishedApi
    internal fun registerChannel(channel: ResultChannelImpl<*, *>) {
        channels[channel.id] = channel
        val result = consumePendingResult(channel.id) ?: return
        channel.consumeResult(result)
    }

    @PublishedApi
    internal fun deregisterChannel(channel: ResultChannelImpl<*, *>) {
        channels.remove(channel.id)
    }

    companion object {
        private val controllerBindings = mutableMapOf<NavigationController, EnroResult>()

        @JvmStatic
        fun from(navigationController: NavigationController): EnroResult {
            return controllerBindings[navigationController]
                ?: throw EnroException.EnroResultIsNotInstalled("EnroResult is not installed")
        }
    }
}