/**
 * Enro Recipe: Module Interceptor
 *
 * Demonstrates a NavigationInterceptor registered on the NavigationComponent,
 * so it sees navigation in every container in the app.
 *
 * [auditInterceptor] is added to `RecipesComponent` with
 * `createNavigationModule { interceptor(auditInterceptor) }`. It handles
 * three events for the keys it names:
 *  - `onOpened` runs before a key is opened
 *  - `onClosed` runs before a key is closed without a result
 *  - `onCompleted` runs before a key completes, and can read its result
 *
 * Each block must end by choosing an outcome: `continueWith...()` lets the
 * operation go ahead, `cancel()` stops it, and `replaceWith(...)` runs a
 * different operation. Here the interceptor records each event and blocks
 * one specific open.
 *
 * To intercept navigation in one container only, pass `interceptor =` to
 * `rememberNavigationContainer`; see the Conditional Navigation recipe.
 */
package dev.enro.recipes.interceptor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ModuleInterceptorRecipe : NavigationKey

@Serializable
data object AuditHome : NavigationKey

@Serializable
data class AuditedDocument(val name: String) : NavigationKey

@Serializable
data object AuditedSignature : NavigationKey.WithResult<String>

// A log of what the interceptor saw, so the recipe screen has something to show.
private val auditLog = mutableStateListOf<String>()

/** Installed in `RecipesComponent`. */
// region docs:interceptors/events
val auditInterceptor = navigationInterceptor {
    onOpened<AuditedDocument> {
        if (key.name == "restricted") {
            // Stop the open. cancelAnd runs the block after cancelling.
            cancelAnd { auditLog.add("Blocked: open ${key.name}") }
        }
        auditLog.add("Opened: ${key.name}")
        continueWithOpen()
    }

    onClosed<AuditedDocument> {
        auditLog.add("Closed: ${key.name}")
        continueWithClose()
    }

    onCompleted<AuditedSignature> {
        // `result` is the value the destination completed with.
        auditLog.add("Signed by: $result")
        continueWithComplete()
    }
}
// endregion

@Composable
@NavigationDestination(ModuleInterceptorRecipe::class)
fun ModuleInterceptorRecipeScreen() {
    val navigation = navigationHandle<ModuleInterceptorRecipe>()
    RecipeScaffold(
        title = "Module Interceptor",
        navigation = navigation,
    ) { modifier ->
        // No interceptor is passed here: the one on the component applies.
        val container = rememberNavigationContainer(
            backstack = backstackOf(AuditHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(AuditHome::class)
fun AuditHomeDestination() {
    val navigation = navigationHandle<AuditHome>()
    val sign = registerForNavigationResult<String> { }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Audit", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.open(AuditedDocument("report")) }) {
            Text("Open 'report'")
        }
        Button(onClick = { navigation.open(AuditedDocument("restricted")) }) {
            Text("Open 'restricted' (blocked by the interceptor)")
        }
        Button(onClick = { sign.open(AuditedSignature) }) {
            Text("Sign")
        }

        Text("Interceptor log:", style = MaterialTheme.typography.titleSmall)
        if (auditLog.isEmpty()) {
            Text("(empty)", style = MaterialTheme.typography.bodySmall)
        } else {
            auditLog.toList().asReversed().forEach {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { auditLog.clear() }) { Text("Clear log") }
        }
    }
}

@Composable
@NavigationDestination(AuditedDocument::class)
fun AuditedDocumentDestination() {
    val navigation = navigationHandle<AuditedDocument>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Document: ${navigation.key.name}", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.close() }) { Text("Close") }
    }
}

@Composable
@NavigationDestination(AuditedSignature::class)
fun AuditedSignatureDestination() {
    val navigation = navigationHandle<AuditedSignature>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Sign", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.complete("Alex") }) { Text("Sign as Alex") }
        Button(onClick = { navigation.close() }) { Text("Cancel") }
    }
}
