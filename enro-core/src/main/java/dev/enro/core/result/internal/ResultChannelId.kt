package dev.enro.core.result.internal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ResultChannelId(
    val ownerId: String,
    val resultId: String
) : Parcelable
