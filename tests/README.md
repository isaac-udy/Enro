# Test Harness

This set of Gradle modules is used as a test harness for Enro. The `:tests:application` module is a module which hosts tests for Enro, such as testing navigation between destinations and how the annotation processor works with incremental changes. 

This application can be used as a place to create situations that reproduce bugs with Enro, and then tests can be written to resolve those bugs. 

The application will pick up any NavigationKey that is public, and can be constructed without arguments (or is an object) and will display these in a list. If you want to add a new destination to the application for testing purposes, simply create a NavigationKey that is able to make be constructed, make sure it's public (not internal) and then launch the application and then launch the NavigationKey from the destination selection screen. 
