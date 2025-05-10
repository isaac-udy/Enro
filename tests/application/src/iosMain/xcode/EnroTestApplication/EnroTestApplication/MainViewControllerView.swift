

import SwiftUI
import UIKit
import EnroTestsApplication

struct MainViewControllerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return Enro.shared.createEnroViewController(
            present: NavigationInstruction.companion.Present(navigationKey: MainView.shared)
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
