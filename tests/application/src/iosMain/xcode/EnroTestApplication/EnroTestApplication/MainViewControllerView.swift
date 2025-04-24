

import SwiftUI
import UIKit
import EnroTestsApplication

struct MainViewControllerView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(generatedModule: EnroExampleAppNavigationReference.shared.reference)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
