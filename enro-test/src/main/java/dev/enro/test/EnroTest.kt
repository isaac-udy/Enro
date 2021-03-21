package dev.enro.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationComponentBuilder
import dev.enro.core.controller.NavigationComponentBuilderCommand
import dev.enro.core.controller.NavigationController
import dev.enro.core.plugins.EnroLogger
import java.io.File
import java.lang.reflect.Field

object EnroTest {
    private val generatedBindings: List<NavigationComponentBuilderCommand> by lazy {
        val isDexClassLoader = Thread.currentThread().contextClassLoader::class.java is BaseDexClassLoader
        val classes = if(isDexClassLoader) getGeneratedClasses() else getGeneratedClassesFromDexFile()

        classes
            .filter {
                NavigationComponentBuilderCommand::class.java.isAssignableFrom(it)
            }
            .map {
                it.newInstance() as NavigationComponentBuilderCommand
            }
            .toList()
    }

    internal fun installNavigationController() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if(application is NavigationApplication) return

        NavigationComponentBuilder()
            .apply { generatedBindings.forEach { it.execute(this) } }
            .apply {
                plugin(EnroLogger())
            }
            .callPrivate<NavigationController>("build")
            .apply { callPrivate("installForTest", application) }
    }

    internal fun uninstallNavigationController() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if(application is NavigationApplication) return
        getCurrentNavigationController().callPrivate<Unit>("uninstall", application)
    }

    fun getCurrentNavigationController(): NavigationController {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if(application is NavigationApplication) return application.navigationController
        return NavigationController.callPrivate("getBoundApplicationForTest", application) as NavigationController
    }
}

private fun getGeneratedClasses(): Sequence<Class<*>> {
    return Thread.currentThread().contextClassLoader
        .getResources("enro_generated_bindings")
        .asSequence()
        .flatMap {
            File(it.file).list().orEmpty().asSequence()
        }
        .filter {
            it.endsWith(".class")
        }
        .toSet()
        .asSequence()
        .mapNotNull {
            runCatching {
                Class.forName("enro_generated_bindings."+it.replace(".class", ""))
            }.getOrNull()
        }
}

private fun getGeneratedClassesFromDexFile(): Sequence<Class<*>> {
    // Here we do some reflection to access the dex files from the class loader. These implementation details vary by platform version,
    // so we have to be a little careful, but not a huge deal since this is just for testing. It should work on 21+.
    // The source for reference is at:
    // https://android.googlesource.com/platform/libcore/+/oreo-release/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
    val classLoader = Thread.currentThread().contextClassLoader as BaseDexClassLoader

    val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
    val pathList = pathListField.get(classLoader) // Type is DexPathList

    val dexElementsField = field("dalvik.system.DexPathList", "dexElements")

    @Suppress("UNCHECKED_CAST")
    val dexElements =
        dexElementsField.get(pathList) as Array<Any> // Type is Array<DexPathList.Element>

    val dexFileField = field("dalvik.system.DexPathList\$Element", "dexFile")
    return dexElements
        .map {
            dexFileField.get(it) as DexFile
        }
        .asSequence()
        .flatMap {
            it.entries().asSequence()
        }
        .filter { it.startsWith("enro_generated_binding") }
        .mapNotNull {
            runCatching { Class.forName(it) }.getOrNull()
        }
}

private fun field(className: String, fieldName: String): Field {
    val clazz = Class.forName(className)
    val field = clazz.getDeclaredField(fieldName)
    field.isAccessible = true
    return field
}

private fun <T> Any.callPrivate(methodName: String, vararg args: Any): T {
    val method = this::class.java.declaredMethods.filter { it.name.startsWith(methodName) }.first()
    method.isAccessible = true
    val result = method.invoke(this, *args)
    method.isAccessible = false
    return result as T
}