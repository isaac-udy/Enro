# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build the project: `./gradlew build`
- Test the project: `./gradlew test`
- Run a specific test: `./gradlew :module:desktopTest --tests "full.class.name.TestName"`
- Run everything CI runs: `./gradlew continuousIntegration` (device tests need an emulator.wtf token)
- Check the public API against the committed dumps: `./gradlew checkKotlinAbi`
- Record an intended public API change: `./gradlew updateKotlinAbi`

## Code Style
- Follow Kotlin official code style with explicit API mode
- Use 4 space indentation
- Follow multiplatform practices, moving code to common module when possible
- Use Kotlin Serialization for serialization
- Navigation components should be annotated with `@NavigationDestination`
- Test classes should use `@RunWith(AndroidJUnit4::class)` and `EnroTestRule`
- Prefer immutable properties with val over var
- Use proper exception handling with runCatching when appropriate
- Follow standard naming conventions: ClassNames in PascalCase, variables/functions in camelCase
- Use meaningful names that describe purpose clearly
- When using annotations like @ExperimentalEnroApi, document reason for usage

## Architecture
- This is a navigation framework for Kotlin multiplatform (focusing on Android)
- Core components include: NavigationKey, NavigationHandle, NavigationContainer, NavigationOperation, NavigationContext