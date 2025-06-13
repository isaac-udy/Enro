package dev.enro.tests.application.samples.travel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Travel-inspired colors
private val TravelBlue = Color(0xFF1E88E5)
private val TravelSkyBlue = Color(0xFF64B5F6)
private val TravelDeepBlue = Color(0xFF0D47A1)
private val TravelTurquoise = Color(0xFF00ACC1)
private val TravelLightTurquoise = Color(0xFF4DD0E1)
private val TravelGreen = Color(0xFF43A047)
private val TravelMint = Color(0xFF81C784)
private val TravelPurple = Color(0xFF5E35B1)
private val TravelLavender = Color(0xFF9575CD)
private val TravelCoral = Color(0xFFFF5252)
private val TravelPink = Color(0xFFFF80AB)

// Neutral colors
private val TravelGray900 = Color(0xFF212121)
private val TravelGray800 = Color(0xFF424242)
private val TravelGray700 = Color(0xFF616161)
private val TravelGray200 = Color(0xFFEEEEEE)
private val TravelGray100 = Color(0xFFF5F5F5)
private val TravelGray50 = Color(0xFFFAFAFA)

// Custom Typography
private val TravelTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = TravelBlue,
    onPrimary = Color.White,
    primaryContainer = TravelSkyBlue.copy(alpha = 0.2f),
    onPrimaryContainer = TravelBlue.copy(alpha = 0.9f),

    secondary = TravelTurquoise,
    onSecondary = Color.White,
    secondaryContainer = TravelLightTurquoise.copy(alpha = 0.3f),
    onSecondaryContainer = TravelDeepBlue,

    tertiary = TravelGreen,
    onTertiary = Color.White,
    tertiaryContainer = TravelMint.copy(alpha = 0.3f),
    onTertiaryContainer = TravelGreen.copy(alpha = 0.9f),

    error = TravelCoral,
    onError = Color.White,
    errorContainer = TravelPink.copy(alpha = 0.2f),
    onErrorContainer = TravelCoral.copy(alpha = 0.9f),

    background = TravelGray50,
    onBackground = TravelGray900,

    surface = Color.White,
    onSurface = TravelGray900,
    surfaceVariant = TravelGray100,
    onSurfaceVariant = TravelGray700,

    outline = TravelGray700.copy(alpha = 0.4f),
    outlineVariant = TravelGray200,

    scrim = Color.Black.copy(alpha = 0.32f)
)

private val DarkColorScheme = darkColorScheme(
    primary = TravelSkyBlue,
    onPrimary = Color(0xFF003258),
    primaryContainer = TravelBlue.copy(alpha = 0.3f),
    onPrimaryContainer = TravelSkyBlue.copy(alpha = 0.9f),

    secondary = TravelLightTurquoise,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = TravelTurquoise.copy(alpha = 0.3f),
    onSecondaryContainer = TravelLightTurquoise.copy(alpha = 0.9f),

    tertiary = TravelMint,
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = TravelGreen.copy(alpha = 0.3f),
    onTertiaryContainer = TravelMint.copy(alpha = 0.9f),

    error = TravelPink,
    onError = Color(0xFF690005),
    errorContainer = TravelCoral.copy(alpha = 0.3f),
    onErrorContainer = TravelPink.copy(alpha = 0.9f),

    background = Color(0xFF121212),
    onBackground = TravelGray200,

    surface = Color(0xFF1E1E1E),
    onSurface = TravelGray200,
    surfaceVariant = TravelGray800,
    onSurfaceVariant = TravelGray200.copy(alpha = 0.8f),

    outline = TravelGray200.copy(alpha = 0.3f),
    outlineVariant = TravelGray800,

    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun TravelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TravelTypography,
        content = content
    )
}
