package dev.enro.tests.application

import dev.enro.tests.application.ios.UIViewControllerComposeDestination
import dev.enro.tests.application.ios.NativeSwiftUIView
import dev.enro.tests.application.ios.NativeUIViewController
import dev.enro.tests.application.ios.UIViewControllerPresentDestination
import dev.enro.tests.application.ios.UIViewControllerPushDestination

@Suppress("unused") // called in Swift code
fun registerIosDestinations() {
    SelectDestination.registerSelectableDestinations(
        NativeSwiftUIView,
        NativeUIViewController,
        UIViewControllerComposeDestination,
        UIViewControllerPresentDestination,
        UIViewControllerPushDestination,
    )
}