import XCTest

// Runs the UIViewController recipe on a simulator: the three ways a
// UIViewController destination can be shown.
final class EnroRecipesUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testUIViewControllerRecipe() throws {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["Enro Recipes"].waitForExistence(timeout: 30))
        openRecipe("UIViewController", in: app)

        // Embedded in the Enro container.
        tap("Embed in this container", in: app)
        // The embedded UIKit views are not in the accessibility tree, so the
        // test checks the host's button, which is only shown while embedded.
        tap("Back to menu", in: app)

        // Presented modally.
        tap("Present modally", in: app)
        XCTAssertTrue(app.staticTexts["Presented modally"].waitForExistence(timeout: 10))
        tap("Close", in: app)
        XCTAssertTrue(app.staticTexts["UIViewController destinations"].waitForExistence(timeout: 10))

        // Pushed onto the UINavigationController.
        tap("Push onto the UINavigationController", in: app)
        XCTAssertTrue(app.staticTexts["Pushed onto the UINavigationController"].waitForExistence(timeout: 10))
        tap("Close", in: app)
        XCTAssertTrue(app.staticTexts["UIViewController destinations"].waitForExistence(timeout: 10))
    }

    // Recipe cards are in a scrolling list; each has a title and an "Open" button.
    private func openRecipe(_ title: String, in app: XCUIApplication) {
        let card = app.staticTexts[title]
        var swipes = 0
        while !(card.exists && card.isHittable) && swipes < 40 {
            app.swipeUp()
            swipes += 1
        }
        XCTAssertTrue(card.exists, "Recipe \(title) not found")
        // A tap during a fling stops the scroll instead of pressing the button.
        sleep(1)
        // The last "Open" button on screen belongs to the last card, which
        // is the platform recipe at the end of the list.
        let open = app.buttons.matching(identifier: "Open")
        open.element(boundBy: open.count - 1).tap()
    }

    private func tap(_ label: String, in app: XCUIApplication) {
        let button = app.buttons[label]
        XCTAssertTrue(button.waitForExistence(timeout: 10), "\(label) not found")
        button.tap()
    }
}
