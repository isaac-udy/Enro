package dev.enro.core.controller

import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.ExperimentalEnroApi
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * This class represents the way in which Enro will handle back navigation that occurs from a back button press.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("EnroBackConfiguration", exact = true)
public sealed interface EnroBackConfiguration {
    /**
     * The Default configuration will listen to back presses in Activities (or in Dialogs), and then forward
     * the back press to the currently active destination.
     */
    public data object Default : EnroBackConfiguration

    /**
     * The Manual configuration will not listen to back presses anywhere, and will require the application to manually
     * call `requestClose` on the correct NavigationHandle when back presses occur.
     */
    @AdvancedEnroApi
    @OptIn(ExperimentalObjCName::class)
    @ObjCName("EnroBackConfigurationManual", exact = true)
    public data object Manual : EnroBackConfiguration

    /**
     * The Predictive configuration integrates Enro with predictive back, allowing for animations to occur during back presses.
     * This involves each individual destination adding it's own back press listener, and only handling it's own back presses.
     *
     * This API is currently an experimental API, as it is not yet fully stabilised.
     *
     * See https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back for more information.
     *
     * Note: Once Predictive back behaviour is stabilised, this will be the default configuration.
     */
    @AdvancedEnroApi
    @ExperimentalEnroApi
    @OptIn(ExperimentalObjCName::class)
    @ObjCName("EnroBackConfigurationPredictive", exact = true)
    public data object Predictive : EnroBackConfiguration
}