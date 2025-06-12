package dev.enro.tests.application

import dev.enro.annotations.NavigationComponent
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.tests.application.samples.loan.ui.LoanPurposeOption
import dev.enro.tests.application.samples.loan.ui.OwnershipOption
import dev.enro.tests.application.samples.loan.ui.PropertyPurposeOption
import dev.enro.tests.application.samples.loan.ui.RepaymentFrequencyOption
import dev.enro.tests.application.samples.loan.ui.RepaymentTypeOption
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@NavigationComponent
object TestApplicationComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        serializersModule(SerializersModule {
            polymorphic(Any::class) {
                subclass(LoanPurposeOption.Car::class)
                subclass(LoanPurposeOption.Property::class)

                subclass(OwnershipOption.Other::class)
                subclass(OwnershipOption.Partner::class)
                subclass(OwnershipOption.Sole::class)

                subclass(PropertyPurposeOption.Investment::class)
                subclass(PropertyPurposeOption.OwnerOccupied::class)

                subclass(RepaymentFrequencyOption.Fortnightly::class)
                subclass(RepaymentFrequencyOption.Monthly::class)
                subclass(RepaymentFrequencyOption.Quarterly::class)

                subclass(RepaymentTypeOption.InterestOnly::class)
                subclass(RepaymentTypeOption.PrincipalAndInterest::class)
            }
        })
    }
)