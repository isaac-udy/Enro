# Documentation Plan for Enro 3.x

This document outlines areas that need documentation for the 3.x release of Enro. Items are
organized by priority and complexity.

## High Priority - Essential Documentation

### 1. Getting Started Guide

**Status:** Complete
**Files:**

- [installation.md](installation.md)
- [basic-concepts.md](basic-concepts.md)

**Completed Content:**
- Installation steps for all platforms (Android, iOS, Desktop, Web/WASM)
- Basic NavigationComponent setup
- Creating first NavigationKey
- Creating first NavigationDestination
- Basic navigation operations (open, close, complete)
- Simple result handling example

### 2. NavigationKeys Documentation

**Status:** Complete
**File:** [navigation-keys.md](navigation-keys.md)

**Completed Content:**
- Defining NavigationKeys with `@Serializable`
- Using `NavigationKey.WithResult<T>` for screens that return results
- NavigationKey.Metadata system and MetadataKeys
- WithMetadata helper functions
- NavigationKey.Instance explained
- Best practices for structuring NavigationKeys
- Migration from 2.x (SupportsPush/SupportsPresent removal)

### 3. NavigationDestinations Documentation

**Status:** Complete
**File:** [navigation-destinations.md](navigation-destinations.md)

**Completed Content:**
- Using `@NavigationDestination` annotation
- Creating Composable destinations
- Creating Fragment destinations (Android via enro-compat)
- Creating Activity destinations (Android via enro-compat)
- Using `navigationDestination` DSL for property-based destinations
- Accessing NavigationHandle in destinations
- Accessing NavigationKey parameters
- Manual binding without annotation processor

### 4. Navigation Operations

**Status:** Complete
**File:** [navigation-operations.md](navigation-operations.md)

**Completed Content:**
- The unified `open()` operation
- Understanding `close()`, `complete()`, and `completeFrom()`
- NavigationOperation types (Open, Close, Complete, CompleteFrom, SideEffect, SetBackstack)
- AggregateOperation for multiple operations
- `closeAndReplaceWith()` and `closeAndCompleteFrom()` helpers
- Migration from push/present/forward/replace

### 5. Result Handling

**Status:** Complete
**File:** [result-handling.md](result-handling.md)

**Completed Content:**
- `registerForNavigationResult` in Composables
- `registerForNavigationResult` in ViewModels
- `registerForNavigationResult` in Fragments (via enro-compat)
- NavigationResultChannel API (open, present capabilities in compat)
- NavigationResultScope with access to instance and key
- Handling closed vs completed results
- Result forwarding with `completeFrom()`

## Medium Priority - Important Features

### 6. Navigation Scenes

**Status:** Needs creation
**Content:**

- What are NavigationScenes and why they exist
- Scene types: SinglePaneScene, DoublePaneScene, DialogScene, DirectOverlayScene
- NavigationSceneStrategy concept
- Using scene metadata: `dialog()`, `directOverlay()`
- Creating custom scenes and strategies
- Multi-pane layouts with DoublePaneScene
- Understanding scene overlays and overlaidEntries
- How scenes replace the old interface-based approach (SupportsPresent, DialogDestination, etc.)

### 7. Navigation Containers

**Status:** Needs comprehensive documentation
**Content:**

- `rememberNavigationContainer` API
- NavigationContainerState and NavigationContainer
- Understanding backstack as List<NavigationKey.Instance>
- EmptyBehavior options (closeParent, allowEmpty, etc.)
- NavigationContainerFilter system (accept, acceptKey, acceptNone, etc.)
- NavigationDisplay composable
- Nested containers
- Container context and finding containers
- Active container management

### 8. Navigation Flows

**Status:** Feature exists, needs documentation update for 3.x
**Content:**

- `registerForFlowResult` API
- Defining multi-step flows
- Flow DSL: `open()`, `async()`, step configuration
- Transient steps with `transient()` and `dependsOn()`
- `alwaysAfterPreviousStep()` configuration
- NavigationFlowReference and `rememberNavigationFlowReference`
- `requireStep()` and editing flow steps
- `rememberNavigationContainerForFlow`
- Use cases: onboarding, wizards, checkout flows
- Example: The recipe creation flow in the test app

### 9. Multiplatform Support

**Status:** New in 3.x, needs creation
**Content:**

- Platform support matrix (Android stable, others experimental)
- Common code patterns
- Platform-specific implementations
- Android-specific features (Fragments, Activities)
- iOS setup and patterns
- Desktop setup and window management
- Web/WASM setup
- Shared navigation logic across platforms
- Platform-specific destinations with @NavigationDestination.PlatformOverride

### 10. Serialization System

**Status:** New in 3.x, needs creation
**Content:**

- Why kotlinx.serialization is used
- Marking NavigationKeys with `@Serializable`
- Registering custom serializers in NavigationModule
- Polymorphic serialization for complex types
- NavigationKey.Metadata serialization
- SerializersModule configuration
- Handling non-serializable types
- Migration from Parcelize

## Lower Priority - Advanced Features

### 11. Navigation Interceptors

**Status:** Feature exists, needs 3.x update
**Content:**

- NavigationInterceptor interface
- Creating custom interceptors
- Interceptor builder DSL
- Use cases: authentication, analytics, logging
- Adding interceptors to NavigationModule
- Order of execution
- AggregateNavigationInterceptor

### 12. Destination Decorators

**Status:** New API, needs documentation
**Content:**

- NavigationDestinationDecorator concept
- Creating custom decorators
- Adding decorators to NavigationModule
- Use cases: theming, error boundaries, loading states
- Built-in decorators
- Decorator composition and ordering

### 13. ViewModels and Navigation

**Status:** Needs update for 3.x
**Content:**

- `navigationHandle()` in ViewModels
- ViewModel scoping with navigation
- `createEnroViewModel` factory
- CreationExtras.navigationHandle
- ViewModelProvider.Factory.withNavigationHandle
- ViewModel lifecycle with navigation
- Sharing ViewModels across destinations

### 14. Path-Based Navigation / Deep Linking

**Status:** Exists but may need update
**Content:**

- NavigationPathBinding
- Pattern matching with PathPattern
- Creating path bindings
- Path parameters and query parameters
- Adding paths to NavigationModule
- Web URL handling
- Android deep link handling

### 15. Testing with Enro

**Status:** Exists (enro-test module) but needs documentation
**Content:**

- EnroTest setup
- `runEnroTest` helper
- Mocking navigation
- Testing navigation flows
- Testing result handling
- Assertions for navigation state
- Best practices

### 16. Animation and Transitions

**Status:** Exists but may need update
**Content:**

- NavigationAnimations
- Animation overrides
- Shared element transitions
- SeekableTransitionState usage
- AnimatedVisibilityScope in destinations
- SharedTransitionScope in destinations
- Platform-specific animations

### 17. Advanced Container Features

**Status:** Needs creation
**Content:**

- Container interceptors
- Finding contexts and containers
- `getChildContext` variations
- Active leaf navigation
- Container debugging
- Performance considerations

### 18. Navigation Plugins

**Status:** Needs documentation
**Content:**

- NavigationPlugin interface
- Creating custom plugins
- Plugin lifecycle (onAttached, onDetached)
- PluginRepository
- Built-in plugins
- Use cases for plugins

### 19. EnroController Configuration

**Status:** Needs creation
**Content:**

- Creating EnroController
- Debug mode
- Strict mode
- Platform-specific configuration
- Module system
- Adding bindings, interceptors, decorators, paths
- SerializersModule configuration

### 20. Fragment and Activity Support (enro-compat)

**Status:** Needs creation for compat layer
**Content:**

- Why enro-compat exists
- Fragment destinations with `@NavigationDestination`
- Activity destinations
- Fragment containers
- registerForNavigationResult in Fragments
- Fragment-to-Composable shared elements (FragmentSharedElements)
- Migration path to Compose

## Documentation Improvements

### General Improvements Needed:

1. **Code samples** - Ensure all examples are tested and work with 3.x
2. **Migration guides** - Clear path from 2.x to 3.x for each major feature
3. **API reference** - Generated KDoc or similar for all public APIs
4. **Architecture diagrams** - Visual representations of key concepts (scenes, containers, flows)
5. **Video tutorials** - Consider short videos for complex topics
6. **Comparison guides** - Compare Enro to Navigation Component, Voyager, etc.
7. **Real-world examples** - Document patterns from the test app (recipes, loans, travel samples)
8. **Troubleshooting guide** - Update existing troubleshooting.md for 3.x
9. **FAQ updates** - Review and update FAQ for 3.x questions
10. **Performance guide** - Best practices for optimal performance

### Sample Applications Documentation:

The test application has excellent examples that should be documented:

- **Recipe Management** - Full CRUD with flows
- **Loan Application** - Complex multi-step flow with branching
- **Travel Sample** - (if exists, document patterns)
- **Serialization Examples** - Both common and platform-specific
- **Bottom Navigation** - Tab-based navigation patterns
- **Shared Element Transitions** - Animation examples
- **Managed Flows** - UserInformationFlow and other flow examples
- **Find Context** - Advanced context navigation patterns
- **Nested Containers** - Complex container hierarchies

## Documentation Platform Considerations

### Current State:

- Documentation site exists at enro.dev
- Old 2.x docs in ghpages-old
- New structure being built in ghpages

### Recommendations:

1. Keep 2.x documentation available at a subdomain or version path
2. Use clear versioning in documentation URLs
3. Add version switcher to documentation site
4. Include "Updated for 3.x" badges on updated pages
5. Add search functionality for documentation
6. Consider using a documentation framework (Docusaurus, MkDocs, etc.)
7. Include interactive examples where possible
8. Add copy-to-clipboard for code samples

## Priority Order for Creation:

### Phase 1 (Essential for 3.x release):

1. Getting Started Guide
2. NavigationKeys Documentation
3. NavigationDestinations Documentation
4. Navigation Operations
5. Result Handling

### Phase 2 (Important for adoption):

6. Navigation Scenes
7. Navigation Containers
8. Navigation Flows
9. Multiplatform Support
10. Serialization System

### Phase 3 (Feature deep-dives):

11. ViewModels and Navigation
12. Fragment and Activity Support (enro-compat)
13. Testing with Enro
14. Path-Based Navigation / Deep Linking

### Phase 4 (Advanced features):

15. Navigation Interceptors
16. Destination Decorators
17. Animation and Transitions
18. Advanced Container Features
19. Navigation Plugins
20. EnroController Configuration

## Notes for Documentation Authors:

- **Consistency**: Use consistent terminology across all documentation
- **Examples**: Every concept should have a working code example
- **Testing**: All code examples should be validated against the test application
- **Cross-linking**: Liberal use of links between related documentation pages
- **Progressive disclosure**: Start simple, then show advanced usage
- **Visual aids**: Diagrams for complex concepts (especially scenes and containers)
- **API stability**: Mark experimental APIs clearly
- **Platform notes**: Call out platform-specific behavior
- **Performance tips**: Include performance considerations where relevant
- **Common pitfalls**: Document common mistakes and how to avoid them
