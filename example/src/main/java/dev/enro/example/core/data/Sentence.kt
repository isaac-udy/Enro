package dev.enro.example.core.data

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationHandle
import kotlin.math.absoluteValue

data class Sentence(
    val adverb: Adverb,
    val adjective: Adjective,
    val noun: Noun,
) {

    fun asCamelCaseString(): String {
        return "${adverb.value}${adjective.value}${noun.value}"
    }

    companion object {
        fun fromId(id: String) : Sentence {
            val hashCode = id.hashCode()
            val adverb = Words.adverbs[hashCode.absoluteValue % Words.adverbs.size]
            val adjective = Words.adjectives[(hashCode.absoluteValue * 3).absoluteValue % Words.adjectives.size]
            val noun = Words.nouns[(hashCode.absoluteValue * 5).absoluteValue % Words.nouns.size]
            return Sentence(
                adverb = adverb,
                adjective = adjective,
                noun = noun,
            )
        }
    }
}

val NavigationHandle.sentenceId: String get() = instruction.sentenceId

val AnyOpenInstruction.sentenceId: String get() {
    return Sentence.fromId(instructionId)
        .asCamelCaseString()
}