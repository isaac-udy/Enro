package dev.enro.processor.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.Resolver
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

sealed class ResolverPlatform {
    class Android(
        val androidApplicationClassName: ClassName,
    ) : ResolverPlatform()

    class Ios(
        val uiApplicationClassName: ClassName,
    ) : ResolverPlatform()

    class JvmDesktop : ResolverPlatform()

    class WasmJs(
        val webDocumentClassName: ClassName,
    ) : ResolverPlatform()

    companion object {
        @OptIn(KspExperimental::class)
        fun getPlatform(resolver: Resolver) : ResolverPlatform {
            val isAndroid = resolver.getKotlinClassByName("dev.enro.platform.EnroPlatformAndroid") != null
            if (isAndroid) {
                return ResolverPlatform.Android(
                    androidApplicationClassName = resolver.getKotlinClassByName("android.app.Application")!!.toClassName()
                )
            }

            val isIos = resolver.getKotlinClassByName("dev.enro.platform.EnroPlatformIOS") != null
            if (isIos) {
                return ResolverPlatform.Ios(
                    uiApplicationClassName = resolver.getKotlinClassByName("platform.UIKit.UIApplication")!!.toClassName()
                )
            }

            val isDesktop = resolver.getKotlinClassByName("dev.enro.platform.EnroPlatformDesktop") != null
            if (isDesktop) {
                return ResolverPlatform.JvmDesktop()
            }

            val isWeb = resolver.getKotlinClassByName("dev.enro.platform.EnroPlatformWasmJs") != null
            if (isWeb) {
                return ResolverPlatform.WasmJs(
                    webDocumentClassName = resolver.getKotlinClassByName("org.w3c.dom.Document")!!.toClassName()
                )
            }

            error("Unknown platform")
        }
    }
}
