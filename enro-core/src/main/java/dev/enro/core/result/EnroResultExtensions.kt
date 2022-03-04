package dev.enro.core.result

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import dev.enro.core.*
import dev.enro.core.result.internal.LazyResultChannelProperty
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelImpl
import dev.enro.core.synthetic.SyntheticDestination
import kotlin.properties.ReadOnlyProperty

fun <T : Any> TypedNavigationHandle<out NavigationKey.WithResult<T>>.closeWithResult(result: T) {
    val resultId = ResultChannelImpl.getResultId(this)
    if (resultId != null) {
        EnroResult.from(controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
    close()
}

fun <T : Any> ExecutorArgs<out Any, out Any, out NavigationKey>.sendResult(
    result: T
) {
    val resultId = ResultChannelImpl.getResultId(instruction)
    if (resultId != null) {
        EnroResult.from(fromContext.controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
}

fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    val resultId = ResultChannelImpl.getResultId(instruction)
    if (resultId != null) {
        EnroResult.from(navigationContext.controller).addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = result::class,
                result = result
            )
        )
    }
}

fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>
) {
    val resultId = ResultChannelImpl.getResultId(instruction)

    // If the incoming instruction does not have a resultId attached, we
    // still want to open the screen we are being forwarded to
    if (resultId == null) {
        navigationContext.getNavigationHandle().executeInstruction(
            NavigationInstruction.Forward(navigationKey)
        )
    } else {
        navigationContext.getNavigationHandle().executeInstruction(
            ResultChannelImpl.overrideResultId(
                NavigationInstruction.Forward(navigationKey), resultId
            )
        )
    }
}

inline fun <reified T : Any> ViewModel.registerForNavigationResult(
    navigationHandle: NavigationHandle,
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Any, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = navigationHandle,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> FragmentActivity.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<FragmentActivity, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> Fragment.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): ReadOnlyProperty<Fragment, EnroResultChannel<T>> =
    LazyResultChannelProperty(
        owner = this,
        resultType = T::class.java,
        onResult = onResult
    )

inline fun <reified T : Any> NavigationHandle.registerForNavigationResult(
    noinline onResult: (T) -> Unit
): EnroResultChannel<T> = ResultChannelImpl(
    navigationHandle = this,
    resultType = T::class.java,
    onResult = onResult
)