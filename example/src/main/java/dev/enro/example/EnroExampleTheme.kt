package dev.enro.example

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp

private val cutiveMono = FontFamily(
    Font(
        resId = R.font.cutive_mono,
        weight = FontWeight.Normal
    )
)

@Composable
fun EnroExampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = Colors(
            primary = Color(0xFF607d8b),
            primaryVariant = Color(0xFF34515e),
            onPrimary = Color.White,

            secondary = Color(0xFFa5f5),
            secondaryVariant = Color(0xFFa5f5),
            onSecondary = Color.White,

            surface = Color.White,
            onSurface = Color(0xFF707070),

            background = Color.White,
            onBackground = Color(0xFF707070),

            error = Color.Red,
            onError = Color.White,

            isLight = true
        ),
        typography = Typography(
            h1 = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = cutiveMono
            ),
            h2 = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = cutiveMono
            ),
            h3 = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = cutiveMono
            ),
            h4 = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = cutiveMono
            ),
            h5 = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = cutiveMono
            ),
            h6 = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = cutiveMono
            ),
            subtitle1 = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                letterSpacing = 0.15.sp
            ),
            subtitle2 = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.1.sp
            ),
            body1 = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            body2 = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
            button = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                letterSpacing = 1.25.sp,
                baselineShift = BaselineShift(.15f),
                fontFeatureSettings = "smcp,c2sc"
            ),
            caption = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = 0.4.sp
            ),
            overline = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )
        )
    ) {
        content()
    }
}