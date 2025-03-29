package dev.enro.core.result.internal

import kotlinx.serialization.Serializable

@Serializable
public data class ResultChannelId(
    val ownerId: String,
    val resultId: String
)
