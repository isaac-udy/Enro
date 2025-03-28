package dev.enro.example.destinations.result.flow.embedded

import android.os.Parcelable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.result.forwardResult
import dev.enro.core.synthetic.syntheticDestination
import dev.enro.example.core.data.Sentence
import kotlinx.parcelize.Parcelize

@Parcelize
class CreateSentenceEmbeddedFlow : Parcelable, NavigationKey.SupportsPush.WithResult<Sentence>

@NavigationDestination(CreateSentenceEmbeddedFlow::class)
val createSentenceEmbeddedFlowDestination = syntheticDestination<CreateSentenceEmbeddedFlow> {
    forwardResult(EmbeddedSelectAdverb)
}