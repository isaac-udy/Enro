package dev.enro.core.result.internal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class ResultChannelId(
    val ownerId: String,
    val resultId: String
) : Parcelable
