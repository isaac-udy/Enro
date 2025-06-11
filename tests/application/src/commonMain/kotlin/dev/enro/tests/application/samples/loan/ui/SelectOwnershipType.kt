package dev.enro.tests.application.samples.loan.ui

import kotlinx.serialization.Serializable


@Serializable
sealed class OwnershipOption : MultiChoiceDestination.Item() {
    @Serializable
    class Sole : OwnershipOption() {
        override val title: String = "Sole Ownership"
    }
    @Serializable
    class Partner : OwnershipOption() {
        override val title: String = "With Partner"
    }
    @Serializable
    class Other : OwnershipOption() {
        override val title: String = "Other"
    }
}

val SelectOwnershipType = MultiChoiceDestination<OwnershipOption>(
    title = "Who's applying?",
    subtitle = "Will this loan be in your name only or with others?",
    items = listOf(
        OwnershipOption.Sole(),
        OwnershipOption.Partner(),
        OwnershipOption.Other()
    )
)
