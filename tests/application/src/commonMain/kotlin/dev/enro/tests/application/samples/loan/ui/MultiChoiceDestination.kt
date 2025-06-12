package dev.enro.tests.application.samples.loan.ui

import dev.enro.NavigationKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MultiChoiceDestination<T: MultiChoiceDestination.Item>(
    val title: String,
    val subtitle: String,
    @Contextual val items: List<T>,
) : NavigationKey.WithResult<T> {

    @Serializable
    abstract class Item {
        abstract val title: String
    }
}
