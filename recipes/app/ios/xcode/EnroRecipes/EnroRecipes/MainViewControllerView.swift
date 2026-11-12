import SwiftUI
import UIKit
import RecipesApp

// Wraps the Kotlin root view controller for SwiftUI. The root view controller
// is placed in a UINavigationController, which Enro requires in order to push
// a `SupportsUINavigationView` destination. The navigation bar is hidden while
// the Compose root is showing, because each recipe draws a top bar.
struct MainViewControllerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let root = MainViewControllerKt.MainViewController()
        let controller = UINavigationController(rootViewController: root)
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator: NSObject, UINavigationControllerDelegate {
        func navigationController(
            _ navigationController: UINavigationController,
            willShow viewController: UIViewController,
            animated: Bool
        ) {
            let isRoot = navigationController.viewControllers.first === viewController
            navigationController.setNavigationBarHidden(isRoot, animated: animated)
        }
    }
}
