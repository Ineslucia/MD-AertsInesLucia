package be.odisee.ineslucia.foodflix.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IconBurgundyDark, // #6B1D2F
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3F0B15),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = FoodFlixPinkLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF521C2B),
    onSecondaryContainer = Color(0xFFFFD9DF),
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFD8D2D2)
)

private val LightColorScheme = lightColorScheme(
    primary = FoodFlixPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9DF),
    onPrimaryContainer = Color(0xFF3F0B15),
    secondary = IconPinkLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFEEF3),
    onSecondaryContainer = Color(0xFF4A1220),
    background = LightBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF7E9EE),
    onSurfaceVariant = Color(0xFF524346)
)

@Composable
fun FoodflixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
