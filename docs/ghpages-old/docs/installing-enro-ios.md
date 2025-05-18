# Installing Enro for iOS

1. Define a "NavigationComponent" in Kotlin code. This can be in the common source set or in the iOS source set, this should be an object which is annotated with @NavigationComponent and extends NavigationComponentConfiguration. Here is an example: 
```kotlin
   @NavigationComponent
   object MyNavigationComponent : NavigationComponentConfiguration(
        // module is an optional parameter, which can be used for configuring the navigation module,
        // and doing things like installing plugins, interceptors, manually adding bindings, etc.
        module = createNavigationModule { /* ... */ }
   )
```

2. In the XCode project, make sure you have configured a UIApplicationDelegate for your application (which should be annotated with `@main`, or referenced from the `@main` SwiftUI View with as `@UIApplicationDelegateAdaptor`). In your func UIApplicationDelegate's `application(_ application:, launchOptions:)` function, you will need to install Enro using the NavigationComponent you declared in step 1. Here is an example:
```swift
@main
class EnroExampleAppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication, 
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {  
        MyNavigationComponent.shared.installNavigationController(
            application: application,
            root: nil,
            strictMode: false,
            useLegacyContainerPresentBehavior: false,
            backConfiguration: Enro.shared.backConfiguration.Default,
            block: { scope in
            }
        )
        return true
    }
}
```

3. From here, you will need to launch an `EnroUIViewController`, like you would launch any other UIViewController. You can do this from the `AppDelegate` or from a SwiftUI View. Here is an example of launching an EnroUIViewController from an `@main` SwiftUI View:
```swift
@main
struct EnroTestApplicationApp: App {
    @UIApplicationDelegateAdaptor(EnroExampleAppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            EnroRootView()
        }
    }
}

struct EnroRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return Enro.shared.createEnroViewController(
            present: NavigationInstruction.companion.Present(navigationKey: YourRootNavigationKey())
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
```

Alternatively, you can skip some of this additional configuration if you're writing an application that
only uses Enro, you can provide a non-nil argument to the `root` parameter of the `installNavigationController` function. This will automatically create a root EnroUIViewController for you, and you won't need to create one yourself. Here is an example:
```swift
@main
class EnroExampleAppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication, 
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {  
        MyNavigationComponent.shared.installNavigationController(
            application: application,
            root: NavigationInstruction.companion.Present(navigationKey: YourRootNavigationKey()), 
            // ...
        )
        return true
    }
}
```
