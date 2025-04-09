package dev.enro.core.activity

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.savedstate.read
import androidx.savedstate.serialization.serializers.ParcelableSerializer
import androidx.savedstate.write
import dev.enro.core.*
import dev.enro.core.compose.navigationHandle
import dev.enro.core.result.AdvancedResultExtensions
import dev.enro.core.synthetic.SyntheticDestinationProvider
import dev.enro.core.synthetic.syntheticDestination
import dev.enro.destination.compose.OverrideNavigationAnimations
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass


public class ActivityResultParameters<I, O : Any, R : Any> internal constructor(
    internal val contract: ActivityResultContract<I, out O?>,
    internal val input: I,
    internal val result: (O) -> R
)

public fun <I, O : Any> ActivityResultContract<I, out O?>.withInput(input: I): ActivityResultParameters<I, O, O> =
    ActivityResultParameters(
        contract = this,
        input = input,
        result = { it }
    )

public fun <I, O : Any, R: Any> ActivityResultParameters<I, O, O>.withMappedResult(block: (O) -> R): ActivityResultParameters<I, O, R> =
    ActivityResultParameters(
        contract = contract,
        input = input,
        result = block
    )

public class ActivityResultDestinationScope<T : NavigationKey.SupportsPresent.WithResult<*>>
    internal constructor(
        public val key: T,
        public val instruction: NavigationInstruction.Open<*>,
        public val context: Context,
        public val activity: ComponentActivity,
    )

@dev.enro.annotations.ExperimentalEnroApi
public fun <R: Any, Key: NavigationKey.SupportsPresent.WithResult<R>> activityResultDestination(
    @Suppress("UNUSED_PARAMETER") // used to infer types
    keyType: KClass<Key>,
    block: ActivityResultDestinationScope<Key>.() -> ActivityResultParameters<*, *, R>
): SyntheticDestinationProvider<Key> = syntheticDestination {
    val scope = ActivityResultDestinationScope(
        key = key,
        instruction = instruction,
        context = navigationContext.activity,
        activity = navigationContext.activity,
    )
    val parameters = scope.block() as ActivityResultParameters<Any, Any, Any>

    val pendingResult = instruction.extras.read { getParcelable<ActivityResult>(PENDING_ACTIVITY_RESULT) }
    if (pendingResult != null) {
        val parsedResult = parameters.contract.parseResult(pendingResult.resultCode, pendingResult.data)
        val mappedResult = parsedResult?.let { parameters.result(it) }
        when (mappedResult) {
            null -> AdvancedResultExtensions.setClosedResultForInstruction(
                navigationController = navigationContext.controller,
                instruction = instruction,
            )
            else -> AdvancedResultExtensions.setResultForInstruction(
                navigationController = navigationContext.controller,
                instruction = instruction,
                result = mappedResult,
            )
        }
        return@syntheticDestination
    }

    val synchronousResult = parameters.contract.getSynchronousResult(navigationContext.activity, parameters.input)
    if (synchronousResult != null) {
        val mappedResult = synchronousResult.value?.let { parameters.result(it) }
        if (mappedResult != null) {
            AdvancedResultExtensions.setResultForInstruction(
                navigationController = navigationContext.controller,
                instruction = instruction,
                result = mappedResult,
            )
            return@syntheticDestination
        }
    }

    navigationContext
        .getNavigationHandle()
        .present(
            ActivityResultDestination(
                wrapped = instruction,
                intent = parameters.contract.createIntent(navigationContext.activity, parameters.input),
            )
        )
}

@PublishedApi
internal const val PENDING_ACTIVITY_RESULT: String = "dev.enro.core.activity.PENDING_ACTIVITY_RESULT"

internal object IntentSerializer : ParcelableSerializer<Intent>()

@Serializable
internal class ActivityResultDestination(
    val wrapped: NavigationInstruction.Open<*>,
    val intent: @Serializable(with = IntentSerializer::class) Intent,
) : NavigationKey.SupportsPresent

@Composable
internal fun ActivityResultBridge() {
    val navigation = navigationHandle<ActivityResultDestination>()
    val launched = rememberSaveable { mutableStateOf(false) }
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        navigation.executeInstruction(
            navigation.key.wrapped.apply {
                extras.write { putParcelable(PENDING_ACTIVITY_RESULT, result) }
            }
        )
        navigation.close()
    }
    LaunchedEffect(Unit) {
        if (launched.value) return@LaunchedEffect
        resultLauncher.launch(navigation.key.intent)
        launched.value = true
    }
    OverrideNavigationAnimations(
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        // No content
    }
}
