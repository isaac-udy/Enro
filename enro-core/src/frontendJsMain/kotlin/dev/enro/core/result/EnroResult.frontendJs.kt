package dev.enro.core.result

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.core.serialization.unwrapForSerialization
import dev.enro.core.serialization.wrapForSerialization
import kotlinx.browser.window
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import org.w3c.dom.BroadcastChannel

// TODO the code below can be used to add cross-window result support if the result type is
//  serializable, but needs some work so isn't currently being used
internal val channel = BroadcastChannel("enro")

private val innerSerializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))
internal fun onResult(result: PendingResult) {
    when (result) {
        is PendingResult.Result -> {
            val map = mapOf(
                "resultId" to result.resultChannelId,
                "instruction" to result.instruction,
                "navigationKey" to result.navigationKey,
                "result" to result.result,
            ).mapValues {
                it.value.wrapForSerialization()
            }
            println(map)
            channel.postMessage(
                NavigationController.jsonConfiguration.encodeToString(innerSerializer, map).toJsString()
            )
        }
        is PendingResult.Closed -> {
            val map = mapOf(
                "resultId" to result.resultChannelId,
                "instruction" to result.instruction,
                "navigationKey" to result.navigationKey,
            ).mapValues {
                it.value.wrapForSerialization()
            }
            println(map)
            channel.postMessage(
                NavigationController.jsonConfiguration.encodeToString(innerSerializer, map).toJsString()
            )
        }
    }
}

public fun listen() {
    channel.onmessage = { event ->
        runCatching {
            val json = event.data as JsString
            println("josn: $json")
            val map = NavigationController.jsonConfiguration
                .decodeFromString(innerSerializer, json.toString())
                .mapValues { it.value.unwrapForSerialization() }
            when {
                map["result"] != null -> {
                    println("result: ${map["result"]}")
                    val result = PendingResult.Result(
                        resultChannelId = map["resultId"] as ResultChannelId,
                        instruction = map["instruction"] as AnyOpenInstruction,
                        navigationKey = map["navigationKey"] as NavigationKey.WithResult<*>,
                        resultType = Any::class,
                        result = map["result"]!!
                    )
                    EnroResult.from(window.navigationController).addPendingResult(result)
                }

                else -> {
                    val result = PendingResult.Closed(
                        resultChannelId = map["resultId"] as ResultChannelId,
                        instruction = map["instruction"] as AnyOpenInstruction,
                        navigationKey = map["navigationKey"] as NavigationKey.WithResult<*>,
                    )
                    EnroResult.from(window.navigationController).addPendingResult(result)
                }
            }
        }.onFailure { it.printStackTrace() }
    }
}