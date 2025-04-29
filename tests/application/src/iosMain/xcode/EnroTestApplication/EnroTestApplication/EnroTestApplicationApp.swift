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
//struct EnroTestApplicationApp: App {
//    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
//
//    var body: some Scene {
//        WindowGroup {
//            MainViewControllerView()
//        }
//    }
//}

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        let root = NavigationInstruction.companion.Present(navigationKey: MainView.shared)
        EnroComponent.shared.installNavigationController(
            application: application,
            root: root,
            strictMode: false,
            useLegacyContainerPresentBehavior: false,
            backConfiguration: Enro.shared.backConfiguration.Default,
            block: { scope in
                // TODO add documentation explaining how to use the Enro iOS bindings
                // TODO add functionality to access navigation handle from UIViewControllers
                // TODO add functionality to access navigation handle from SwiftUI views
//                Enro.shared.addUIViewControllerNavigationBinding(
//                    scope: scope,
//                    key: MainView.shared,
//                    constructDestination: {
//                        MainViewControllerKt.MainViewController()
//                    }
//                )
            }
        )
        return true
    }
}
