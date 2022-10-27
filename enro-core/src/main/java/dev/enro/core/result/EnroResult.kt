package dev.enro.core.result

import dev.enro.core.*
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

    override fun onActive(navigationHandle: NavigationHandle) {
        channels.values
            .filter { channel ->
                pendingResults.any { it.key == channel.id }
            }
            .forEach {
                val result = consumePendingResult(it.id) ?: return@forEach
                it.consumeResult(result.result)
            }
    }

    internal fun addPendingResultFromContext(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Close
    ) {
        if (instruction !is NavigationInstruction.Close.WithResult) return
        val openInstruction = navigationContext.arguments.readOpenInstruction() ?: return
        val resultId = openInstruction.internal.resultId ?: when {
            navigationContext.controller.isInTest ->  ResultChannelId(
                ownerId = openInstruction.instructionId,
                resultId = openInstruction.instructionId
            )
            else -> return
        }
        addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = instruction.result::class,
                result = instruction.result,
            )
        )
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

    @PublishedApi
    internal fun registerChannel(channel: ResultChannelImpl<*, *>) {
        channels[channel.id] = channel
        val result = consumePendingResult(channel.id) ?: return
        channel.consumeResult(result.result)
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