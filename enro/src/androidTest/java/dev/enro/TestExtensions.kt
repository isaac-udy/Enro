package dev.enro

import android.app.Activity
import android.app.Application
import android.os.Debug
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.result.EnroResultChannel
import kotlin.reflect.KClass

private val isDebugging: Boolean get() = Debug.isDebuggerConnected()

inline fun <reified T: NavigationKey> ActivityScenario<out ComponentActivity>.getNavigationHandle(): TypedNavigationHandle<T> {
    var result: NavigationHandle? = null
    onActivity{
        result = it.getNavigationHandle()
    }

    val handle = result ?: throw IllegalStateException("Could not retrieve NavigationHandle from Activity")
    handle.key as? T
        ?: throw IllegalStateException("Handle was of incorrect type. Expected ${T::class.java.name} but was ${handle.key::class.java.name}")
    return handle.asTyped()
}

class TestNavigationContext<Context: Any, KeyType: NavigationKey>(
    val context: Context,
    val navigation: TypedNavigationHandle<KeyType>
) {
    val navigationContext = kotlin.run {
        navigation.getPrivate<NavigationHandle>("navigationHandle")
            .getPrivate<NavigationContext<*>>("navigationContext")
    }
}

inline fun <reified KeyType: NavigationKey> expectComposableContext(
    noinline selector: (TestNavigationContext<ComposableDestination, KeyType>) -> Boolean = { true }
): TestNavigationContext<ComposableDestination, KeyType> {
    return expectContext(selector)
}

inline fun <reified KeyType: NavigationKey> expectFragmentContext(
    noinline selector: (TestNavigationContext<Fragment, KeyType>) -> Boolean = { true }
): TestNavigationContext<Fragment, KeyType> {
    return expectContext(selector)
}

inline fun <reified ContextType: Any, reified KeyType: NavigationKey> findContextFrom(
    rootContext: NavigationContext<*>?,
    noinline selector: (TestNavigationContext<ContextType, KeyType>) -> Boolean = { true }
): TestNavigationContext<ContextType, KeyType>? = findContextFrom(ContextType::class, KeyType::class, rootContext, selector)

fun <ContextType: Any, KeyType: NavigationKey> findContextFrom(
    contextType: KClass<ContextType>,
    keyType: KClass<KeyType>,
    rootContext: NavigationContext<*>?,
    selector: (TestNavigationContext<ContextType, KeyType>) -> Boolean = { true }
): TestNavigationContext<ContextType, KeyType>? {
    var activeContext = rootContext
    while(activeContext != null) {
        if (
            keyType.java.isAssignableFrom(activeContext.getNavigationHandle().key::class.java)
            && contextType.java.isAssignableFrom(activeContext.contextReference::class.java)
        ) {
            val context = TestNavigationContext(
                activeContext.contextReference as ContextType,
                activeContext.getNavigationHandle().asTyped(keyType)
            )
            if (selector(context)) return context
        }

        activeContext.containerManager.containers
            .filter { it.acceptsDirection(NavigationDirection.Present) }
            .forEach { presentationContainer ->
                presentationContainer.activeContext
                    ?.let {
                        findContextFrom(contextType, keyType, it, selector)
                    }
                    ?.let {
                        return it
                    }
            }

        activeContext = activeContext.containerManager.activeContainer?.activeContext
            ?: when(val reference = activeContext.contextReference) {
                is FragmentActivity -> reference.supportFragmentManager.primaryNavigationFragment?.navigationContext
                is Fragment -> reference.childFragmentManager.primaryNavigationFragment?.navigationContext
                else -> null
            }
    }
    return null
}

inline fun <reified ContextType: Any, reified KeyType: NavigationKey> expectContext(
    noinline selector: (TestNavigationContext<ContextType, KeyType>) -> Boolean = { true }
): TestNavigationContext<ContextType, KeyType> {

    return when {
        ComposableDestination::class.java.isAssignableFrom(ContextType::class.java) ||
        Fragment::class.java.isAssignableFrom(ContextType::class.java) -> {
            waitOnMain {
                val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                val activity = activities.firstOrNull() as? ComponentActivity ?: return@waitOnMain null

                return@waitOnMain findContextFrom(activity.navigationContext, selector)
            }
        }
        ComponentActivity::class.java.isAssignableFrom(ContextType::class.java) -> waitOnMain {
            val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val activity = activities.firstOrNull()
            if(activity !is ComponentActivity) return@waitOnMain null
            if(activity !is ContextType) return@waitOnMain null

            val context = TestNavigationContext(
                activity as ContextType,
                activity.getNavigationHandle().asTyped<KeyType>()
            )
            return@waitOnMain if(selector(context)) context else null
        }
        else -> throw RuntimeException("Failed to get context type ${ContextType::class.java.name}")
    }
}


fun getActiveActivity(): Activity? {
    val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
    return activities.firstOrNull()
}

fun expectActivityHostForAnyInstruction(): FragmentActivity {
    return expectActivity { it::class.java.simpleName == "ActivityHostForAnyInstruction" }
}

fun expectFragmentHostForPresentableFragment(): Fragment {
    return expectFragment { it::class.java.simpleName == "FragmentHostForPresentableFragment" }
}

inline fun <reified T: ComponentActivity> expectActivity(crossinline selector: (ComponentActivity) -> Boolean = { it is T }): T {
    return expectContext<T, NavigationKey> {
        selector(it.context)
    }.context
}

internal inline fun <reified T: Fragment> expectFragment(crossinline selector: (T) -> Boolean = { true }): T {
    return expectContext<T, NavigationKey> {
        selector(it.context)
    }.context
}

internal inline fun <reified T: Fragment> expectNoFragment(crossinline selector: (Fragment) -> Boolean = { it is T }): Boolean {
    waitFor {
        runCatching { expectFragment<T>(selector) }.isFailure
    }
    return true
}

internal inline fun <reified T: NavigationKey> expectNoComposableContext(
    noinline selector: (TestNavigationContext<ComposableDestination, T>
) -> Boolean = { true }): Boolean {
    waitFor {
        runCatching { expectComposableContext(selector) }.isFailure
    }
    return true
}

internal inline fun <reified T: NavigationKey> expectNoFragmentContext(
    noinline selector: (TestNavigationContext<Fragment, T>
    ) -> Boolean = { true }): Boolean {
    waitFor {
        runCatching { expectFragmentContext(selector) }.isFailure
    }
    return true
}

fun expectNoActivity() {
    waitOnMain {
        val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.PRE_ON_CREATE).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.CREATED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.STARTED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.PAUSED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.STOPPED).toList() +
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESTARTED).toList()
        return@waitOnMain if(activities.isEmpty()) true else null
    }
}

fun waitFor(block: () -> Boolean) {
    val maximumTime = 3_000
    val startTime = System.currentTimeMillis()

    while(true) {
        if(block()) return
        Thread.sleep(33)
        if(System.currentTimeMillis() - startTime > maximumTime) throw IllegalStateException("Took too long waiting")
    }
}

fun <T: Any> waitOnMain(block: () -> T?): T {
    if(isDebugging) { Thread.sleep(2000) }

    val maximumTime = 7_000
    val startTime = System.currentTimeMillis()
    var currentResponse: T? = null

    while(true) {
        if (System.currentTimeMillis() - startTime > maximumTime) throw IllegalStateException("Took too long waiting")
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            currentResponse = block()
        }
        currentResponse?.let { return it }
        Thread.sleep(33)
    }
}

fun getActiveEnroResultChannels(): List<EnroResultChannel<*, *>> {
    val enroResultClass = Class.forName("dev.enro.core.result.EnroResult")
    val getEnroResult = enroResultClass.getDeclaredMethod("from", NavigationController::class.java)
    getEnroResult.isAccessible = true
    val enroResult = getEnroResult.invoke(null, application.navigationController)
    getEnroResult.isAccessible = false

    requireNotNull(enroResult)
    val channels = enroResult.getPrivate<Map<Any, EnroResultChannel<*, * >>>("channels")
    return channels.values.toList()
}

fun clearAllEnroResultChannels() {
    val enroResultClass = Class.forName("dev.enro.core.result.EnroResult")
    val getEnroResult = enroResultClass.getDeclaredMethod("from", NavigationController::class.java)
    getEnroResult.isAccessible = true
    val enroResult = getEnroResult.invoke(null, application.navigationController)
    getEnroResult.isAccessible = false

    requireNotNull(enroResult)
    val channels = enroResult.getPrivate<MutableMap<Any, EnroResultChannel<*, * >>>("channels")
    channels.clear()
}

@Suppress("unused")
fun <T> Any.callPrivate(methodName: String, vararg args: Any): T {
    val method = this::class.java.declaredMethods.first { it.name.startsWith(methodName) }
    method.isAccessible = true
    val result = method.invoke(this, *args)
    method.isAccessible = false

    @Suppress("UNCHECKED_CAST")
    return result as T
}

fun <T> Any.getPrivate(methodName: String): T {
    val method = this::class.java.declaredFields.first { it.name.startsWith(methodName) }
    method.isAccessible = true
    val result = method.get(this)
    method.isAccessible = false

    @Suppress("UNCHECKED_CAST")
    return result as T
}

val application: Application get() =
    InstrumentationRegistry.getInstrumentation().context.applicationContext as Application

val ComponentActivity.navigationContext get() =
    getNavigationHandle().getPrivate<NavigationContext<*>>("navigationContext")

val Fragment.navigationContext get() =
    getNavigationHandle().getPrivate<NavigationContext<*>>("navigationContext")