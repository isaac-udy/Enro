# Architecture
The purpose of this documentation is to provide information on how Enro works internally.

## Goals
The goal of Enro is to allow navigation between the screens of an application without requiring the a screen to have knowledge about the implementation of the screens that it wants to navigate to. Fundamentally, this means that the contract for each screen must be able to be created before the screen has been implemented, and must be able to live in a separate compilation unit to the screen's implementation. A screen implementation should depend on the contract, but the contract should not know about the screen.

In addition to this key goal of Enro, there are several additional additional goals:
- Allow many different types of screens to be used in Enro (Activity, Fragment, Composable) and allow these screens to interoperate as seamlessly as possible.
- Allow the contracts for screens to be strongly typed
- Allow the contracts for screens to define an output type and return results of that output type
- Allow navigation to (optionally) be managed at the ViewModel layer of an application
- Generate as much of the navigation infrastructure code as possible


## Core Concepts
Enro has several named concepts which need to be understood.

### NavigationDestination
A NavigationDestination is a "screen" within an application. Generally these are implemented as an Activity, Fragment, or Composable function. A NavigationDestination is bound to a particular NavigationKey. A NavigationDestination is a concept, not an actual object, and is represented by an annotation in Enro. This annotation takes a NavigationKey class as an argument, and represents that the Activity/Fragment/Composable *is a* NavigationDestination for a particular NavigationKey.

#### SyntheticDestination
A "SyntheticDestination" is a special type of NavigationDestination. A SyntheticDestination is an interface, which can be declared as a NavigationDestination for a particular NavigationKey and will be invoked whenever navigation to that NavigationKey is requested. This can be used as a bridge between Enro and external libraries or to perform some custom action.

Example: A SyntheticDestination which handles a NavigationKey called "ExternalLink(val url: String)", which uses the androidx.browser library to launch a browser session to the provided url.

Example: A SyntheticDestination which handles a NavigationKey called "NotImplementedYet", which does nothing on the screen, but prints a message in the console saying that the destination has not been implemented.

Example: A NavigationDestination in your application has been re-written, and you want to A/B test the new implementation against the old implementation. A SyntheticDestination could be defined which would launch either the old or the new NavigationDestination based on some criteria.

### NavigationKey
A NavigationKey represents the contract for a particular NavigationDestination and are the objects used to perform navigation. A NavigationKey defines the inputs/arguments for a NavigationDestination.

A NavigationKey must be parcelable, as it will be stored in bundles for Activity Intents or Fragment Arguments, and will be written to saved instance state bundles.

### NavigationHandle
A NavigationHandle is the object that controls navigation within a particular NavigationDestination. A NavigationHandle has a reference to the NavigationKey that was used to open the NavigationDestination they are associated with. A NavigationHandle is what is used to perform navigation by executing NavigationInstructions.

### NavigationContext
A NavigationContext represents a reference to a Fragment, Activity or Composable in which navigation can occur.

### NavigationInstruction
A NavigationInstruction represents some action that a particular NavigationHandle should perform. Currently, there are three top level types of NavigationInstruction: Open, Close, and RequestClose.

#### Open

A NavigationInstruction.Open opens the NavigationDestination associated with a particular
NavigationKey.

#### Close

A NavigationInstruction.Close closes the NavigationDestination that is associated with the
NavigationHandle the instruction is executed on.

#### RequestClose

A NavigationInstruction.RequestClose requests that the NavigationHandle it is executed on performs a
NavigationInstruction.Close action. This is a "softer" version of the close request, and is executed
by things such as a user pressing the "back" key. NavigationHandles can be configured to perform a
custom action when a RequestClose instruction is executed. For example, this might be used to
confirm that unsaved changes will be discarded before the NavigationDestination is actually closed.

### NavigationBinding

A NavigationBinding is an that is used to directly represent binding between a NavigationKey type
and a NavigationDestination type.

### NavigationExecutor

A NavigationExecutor is the object that executes NavigationInstructions.

When a NavigationInstruction.Open is executed, the NavigationController finds the appropriate
NavigationExecutor and provides it with the NavigationInstruction.Open that is being executed, the
NavigationContext in which the instruction is being executed, and the NavigationBinding that
contains the NavigationDestination type. It is then the responsibility of the NavigationExecutor to
open that NavigationDestination.

When a NavigationInstruction.Close is executed, the NavigationController finds the appropriate
NavigationExecutor and provides it with the NavigationContext in which the instruction is being
executed. It is then the responsibility of the NavigationExecutor to close that NavigationContext
appropriately.

### NavigationController

The NavigationController is a Singleton object which is bound to the Application's lifecycle. The
NavigationController stores all the NavigationBindings and NavigationExecutors for the application.


