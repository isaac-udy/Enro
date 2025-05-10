//
//  EnroTestApplicationApp.swift
//  EnroTestApplication
//
//  Created by Isaac Udy on 24/04/25.
//

import Foundation
import SwiftUI
import EnroTestsApplication

//@main
struct EnroTestApplicationApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            MainViewControllerView()
        }
    }
}

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        SelectDestination_iosKt.registerIosDestinations()
        EnroComponent.shared.installNavigationController(
            application: application,
            root: NavigationInstruction.companion.Present(navigationKey: MainView.shared),
            strictMode: false,
            useLegacyContainerPresentBehavior: false,
            backConfiguration: Enro.shared.backConfiguration.Default,
            block: { scope in
                Enro.shared.addUIViewControllerNavigationBinding(
                    scope: scope,
                    keyType: NativeSwiftUIView.self,
                    constructDestination: {
                        UIHostingController(rootView: NativeSwiftUIViewDestination())
                    }
                )
            }
        )
        return true
    }
}


struct NativeSwiftUIViewDestination: View {
    var body: some View {
        VStack {
            Text("This is a SwiftUI View hosted as an Enro destination")
        }
        .padding()
    }
}

