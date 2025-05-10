package dev.enro.tests.application

import dev.enro.tests.application.ios.NativeSwiftUIView
import dev.enro.tests.application.ios.NativeUIViewController

@Suppress("unused") // called in Swift code
fun registerIosDestinations() {
    SelectDestination.registerSelectableDestinations(
        NativeSwiftUIView,
        NativeUIViewController,
    )
}