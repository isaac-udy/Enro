import SwiftUI
import UIKit
import RecipesApp

// The iOS host for the Enro recipes. The recipes are Kotlin code in
// :recipes:common; the iOS app installs Enro and shows the root view controller.
@main
struct EnroRecipesApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            MainViewControllerView()
                .ignoresSafeArea()
        }
    }
}

class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Enro must be installed before any view controller is created.
        MainViewControllerKt.installRecipes(application: application)
        return true
    }
}
