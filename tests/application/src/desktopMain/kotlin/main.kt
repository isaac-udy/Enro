
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.application
import dev.enro.annotations.NavigationComponent
import dev.enro.asInstance
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.desktop.RootWindow
import dev.enro.desktop.openWindow
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.samples.loan.ui.LoanPurposeOption
import dev.enro.tests.application.samples.loan.ui.OwnershipOption
import dev.enro.tests.application.samples.loan.ui.PropertyPurposeOption
import dev.enro.tests.application.samples.loan.ui.RepaymentFrequencyOption
import dev.enro.tests.application.samples.loan.ui.RepaymentTypeOption
import dev.enro.ui.EnroApplicationContent
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@NavigationComponent
object ExampleApplicationNavigation : NavigationComponentConfiguration()

fun main() {
    val controller = ExampleApplicationNavigation.installNavigationController(Unit) {
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
    controller.openWindow(TestApplicationWindow())
    application {
        EnroApplicationContent(controller)
    }
}

class TestApplicationWindow() : RootWindow() {
    init {
        windowConfiguration = WindowConfiguration(
            title = "Enro Test Application",
            onCloseRequest = { close() },
            onKeyEvent = {
                if (it.type == KeyEventType.KeyDown && it.key == Key.W && it.isMetaPressed) {
                    close()
                    true
                }
                if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                    backDispatcher.onBack()
                }
                false
            }
        )
    }

    @Composable
    override fun FrameWindowScope.Content() {
        MenuBar {
            Menu("Window") {
                Item(
                    "Back",
                    shortcut = KeyShortcut(
                        key = Key.LeftBracket,
                        meta = true
                    )
                ) {
                    backDispatcher.onBack()
                }
                Item(
                    "Close",
                    shortcut = KeyShortcut(
                        key = Key.W,
                        meta = true
                    )
                ) {
                    close()
                }
            }
        }
        val container = rememberNavigationContainer(
            backstack = listOf(SelectDestination().asInstance())
        )
        NavigationDisplay(container)
    }
}