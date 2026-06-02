

import SwiftUI
import UIKit
import EnroTestsApplication

struct MainViewControllerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = UINavigationController()
        controller.viewControllers = [MainViewControllerKt.MainViewController()]
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
