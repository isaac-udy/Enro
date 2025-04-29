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
        let root = Enro_coreNavigationInstruction.companion.Present(navigationKey: MainView.shared)
        EnroComponent.shared.installNavigationController(
            application: application,
            root: root,
            strictMode: false,
            useLegacyContainerPresentBehavior: false,
            backConfiguration: EnroBackConfigurationDefault.shared,
            block: { scope in
                
            }
        )
        return true
    }

//    // MARK: UISceneSession Lifecycle
//
//    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
//        // Called when a new scene session is being created.
//        // Use this method to select a configuration to create the new scene with.
//        print("CONNECTING")
//        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
//    }
//
//    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
//        // Called when the user discards a scene session.
//        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
//        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
//    }


}
