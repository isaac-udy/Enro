package dev.enro.test.extensions

import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.test.EnroTest
import kotlin.reflect.KClass

fun <T: Any> NavigationInstruction.Open<*>.sendResultForTest(type: Class<T>, result: T) {
    val navigationController = EnroTest.getCurrentNavigationController()

    val resultChannelClass = Class.forName("dev.enro.core.result.internal.ResultChannelImplKt")
    val getResultId = resultChannelClass.getDeclaredMethod("getResultId", NavigationInstruction.Open::class.java)
    getResultId.isAccessible = true
    val resultId = getResultId.invoke(null, this)
    getResultId.isAccessible = false

    val pendingResultClass = Class.forName("dev.enro.core.result.internal.PendingResult")
    val pendingResultConstructor = pendingResultClass.getDeclaredConstructor(
        resultId::class.java,
        KClass::class.java,
        Any::class.java
    )
    val pendingResult = pendingResultConstructor.newInstance(resultId, type.kotlin, result)

    val enroResultClass = Class.forName("dev.enro.core.result.EnroResult")
    val getEnroResult = enroResultClass.getDeclaredMethod("from", NavigationController::class.java)
    getEnroResult.isAccessible = true
    val enroResult = getEnroResult.invoke(null, navigationController)
    getEnroResult.isAccessible = false

    val addPendingResult = enroResultClass.declaredMethods.first { it.name.startsWith("addPendingResult") }
    addPendingResult.isAccessible = true
    addPendingResult.invoke(enroResult, pendingResult)
    addPendingResult.isAccessible = false
}

inline fun <reified T: Any> NavigationInstruction.Open<*>.sendResultForTest(result: T) {
    sendResultForTest(T::class.java, result)
}

@Suppress("UNCHECKED_CAST")
internal fun getTestResultForId(id: String): Any? {
    val navigationController = EnroTest.getCurrentNavigationController()

    val enroResultClass = Class.forName("dev.enro.core.result.EnroResult")
    val getEnroResult = enroResultClass.getDeclaredMethod("from", NavigationController::class.java)
    getEnroResult.isAccessible = true
    val enroResult = getEnroResult.invoke(null, navigationController)
    getEnroResult.isAccessible = false

    val addPendingResult = enroResultClass.declaredFields.first { it.name.startsWith("pendingResults") }
    addPendingResult.isAccessible = true
    val results = addPendingResult.get(enroResult) as Map<ResultChannelId, Any>
    addPendingResult.isAccessible = false

    val resultChannelId = ResultChannelId(ownerId = id, resultId = id)
    val result = results[resultChannelId] ?: return null

    val pendingResultClass = Class.forName("dev.enro.core.result.internal.PendingResult")
    val resultField = pendingResultClass.declaredFields.first { it.name == "result" }
    resultField.isAccessible = true
    return resultField.get(result)
}
