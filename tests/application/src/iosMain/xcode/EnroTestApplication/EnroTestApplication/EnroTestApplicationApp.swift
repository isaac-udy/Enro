//
//  EnroTestApplicationApp.swift
//  EnroTestApplication
//
//  Created by Isaac Udy on 24/04/25.
//

import Foundation
import SwiftUI
import UIKit
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
                
                Enro.shared.addUIViewControllerNavigationBinding(
                    scope: scope,
                    keyType: NativeUIViewController.self,
                    constructDestination: { CodeBasedViewController() }
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

class CodeBasedViewController: UIViewController {

    // MARK: - Properties

    let titleLabel: UITextView = {
        let label = UITextView()
        label.translatesAutoresizingMaskIntoConstraints = false // Enable Auto Layout
        label.text = "This is a UIViewController hosted as an Enro destination"
        label.font = UIFont.systemFont(ofSize: 15)
        label.textAlignment = .center
        label.isEditable = false
        label.isScrollEnabled = false
        return label
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white // Set the background color

        // Add the subviews to the main view
        view.addSubview(titleLabel)

        // Set up Auto Layout constraints
        setupConstraints()
    }

    // MARK: - Layout

    private func setupConstraints() {
        NSLayoutConstraint.activate([
            // Title Label Constraints
            titleLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            titleLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 40),
            titleLabel.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 20),
            titleLabel.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -20),
        ])
    }
}
