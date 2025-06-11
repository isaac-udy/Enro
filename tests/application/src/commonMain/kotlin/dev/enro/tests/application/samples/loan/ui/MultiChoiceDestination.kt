package dev.enro.tests.application.samples.loan.ui

import dev.enro.NavigationKey
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class MultiChoiceDestination<T: MultiChoiceDestination.Item>(
    val items: List<@Polymorphic T>,
) : NavigationKey.WithResult<T> {

    @Serializable
    abstract class Item {
        abstract val title: String
    }
}