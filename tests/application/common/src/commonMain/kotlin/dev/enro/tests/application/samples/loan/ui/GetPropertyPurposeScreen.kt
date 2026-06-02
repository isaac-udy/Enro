package dev.enro.tests.application.samples.loan.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class PropertyPurposeOption : MultiChoiceDestination.Item() {
    @Serializable
    class Investment : PropertyPurposeOption() {
        override val title: String = "Investment"
    }

    @Serializable
    class OwnerOccupied : PropertyPurposeOption() {
        override val title: String = "Owner Occupied"
    }
}

val GetPropertyPurposeScreen = MultiChoiceDestination(
    title = "Property type",
    subtitle = "Will you live in it or rent it out?",
    items = listOf(
        PropertyPurposeOption.Investment(),
        PropertyPurposeOption.OwnerOccupied()
    )
)