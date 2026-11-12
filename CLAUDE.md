# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build the project: `./gradlew build`
- Test the project: `./gradlew test`
- Run a specific test: `./gradlew :module:desktopTest --tests "full.class.name.TestName"`
- Run everything CI runs: `./gradlew continuousIntegration` (device tests need an emulator.wtf token)
- Check the public API against the committed dumps: `./gradlew checkKotlinAbi`
- Record an intended public API change: `./gradlew updateKotlinAbi`
- Check the docs code samples against the compiled snippets: `./gradlew checkDocsSnippets`
- Rewrite the docs code samples from the snippets: `./gradlew updateDocsSnippets`

## Docs code samples
- The docs site source is `docs/ghpages`. Its Kotlin samples live as real code in `docs/snippets`.
- A sample is a `// region docs:<page>/<name>` … `// endregion` block. The Markdown binds a fenced
  block to it with `<!-- snippet: <page>/<name> -->` on the line above the fence.
- To change a bound sample, edit the Kotlin and run `updateDocsSnippets`; do not edit the Markdown block.
- A line ending in `// docs:hide` is compiled but left out of the docs.
- Mark a block that cannot be compiled (Gradle scripts, Enro 2 code) `<!-- snippet: skip (reason) -->`.
- `checkDocsSnippets` fails on a Kotlin block that has neither marker.

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