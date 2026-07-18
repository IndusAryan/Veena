package com.indus.veena.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val PinkLotus       = Color(0xFFA84860)
val BeigeManuscript = Color(0xFFBCAAA4)
val BlueWater       = Color(0xFF5890A0)
val BrownPot        = Color(0xFFB06050)
val YellowDrape     = Color(0xFFF9A825)
val GreenBenzaiten  = Color(0xFF96D5A6)
val GoldCrown       = Color(0xFFFFB300)

enum class VeenaAccent(val color: Color, val displayName: String) {
    MATERIAL_YOU(Color.Unspecified, "Material You"),
    PINK_LOTUS(PinkLotus, "Pink Lotus"),
    BEIGE_MANUSCRIPT(BeigeManuscript, "Beige Script"),
    BLUE_WATER(BlueWater, "Blue Sea"),
    BROWN_POT(BrownPot, "Brown Pot"),
    YELLOW_DRAPE(YellowDrape, "Yellow Drape"),
    GREEN_BENZAITEN(GreenBenzaiten, "Green Benzaiten"),
    GOLD_CROWN(GoldCrown, "Gold Crown")
}

private object PinkLotusScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF9C4258),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9DF),
        onPrimaryContainer = Color(0xFF3F0014),
        secondary        = Color(0xFF75565B),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9DF),
        onSecondaryContainer = Color(0xFF2B1519),
        tertiary         = Color(0xFF7A5733),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDDB1),
        onTertiaryContainer = Color(0xFF2B1700),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF201A1B),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF201A1B),
        surfaceVariant   = Color(0xFFF3DDE1),
        onSurfaceVariant = Color(0xFF514347),
        outline          = Color(0xFF847377)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFA84860),
        onPrimary        = Color(0xFF60122B),
        primaryContainer = Color(0xFF7E2A41),
        onPrimaryContainer = Color(0xFFFFD9DF),
        secondary        = Color(0xFFE4BDC2),
        onSecondary      = Color(0xFF43292E),
        secondaryContainer = Color(0xFF5B3F44),
        onSecondaryContainer = Color(0xFFFFD9DF),
        tertiary         = Color(0xFFEBBF90),
        onTertiary       = Color(0xFF462A09),
        tertiaryContainer = Color(0xFF5F401D),
        onTertiaryContainer = Color(0xFFFFDDB1),
        background       = Color(0xFF0D0D0D),
        onBackground     = Color(0xFFEDE0E1),
        surface          = Color(0xFF0D0D0D),
        onSurface        = Color(0xFFEDE0E1),
        surfaceVariant   = Color(0xFF514347),
        onSurfaceVariant = Color(0xFFD5C2C6),
        outline          = Color(0xFF9E8C90)
    )
}

private object BeigeManuscriptScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF6D5B52),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF5DED7),
        onPrimaryContainer = Color(0xFF271712),
        secondary        = Color(0xFF705C58),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFADDD9),
        onSecondaryContainer = Color(0xFF2B1917),
        tertiary         = Color(0xFF5F5F30),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE6E3A8),
        onTertiaryContainer = Color(0xFF1C1C00),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF201A19),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF201A19),
        surfaceVariant   = Color(0xFFF4DED9),
        onSurfaceVariant = Color(0xFF52443F),
        outline          = Color(0xFF84746F)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFD8BDB6),
        onPrimary        = Color(0xFF3D2D27),
        primaryContainer = Color(0xFF54433D),
        onPrimaryContainer = Color(0xFFF5DED7),
        secondary        = Color(0xFFDDC2BE),
        onSecondary      = Color(0xFF3F2B29),
        secondaryContainer = Color(0xFF57413E),
        onSecondaryContainer = Color(0xFFFADDD9),
        tertiary         = Color(0xFFC9C78E),
        onTertiary       = Color(0xFF313107),
        tertiaryContainer = Color(0xFF48481C),
        onTertiaryContainer = Color(0xFFE6E3A8),
        background       = Color(0xFF201A19),
        onBackground     = Color(0xFFEDE0DD),
        surface          = Color(0xFF201A19),
        onSurface        = Color(0xFFEDE0DD),
        surfaceVariant   = Color(0xFF52443F),
        onSurfaceVariant = Color(0xFFD7C3BE),
        outline          = Color(0xFFA08D89)
    )
}

private object BlueWaterScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF00687A),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFAAEDFF),
        onPrimaryContainer = Color(0xFF001F26),
        secondary        = Color(0xFF4B6269),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCEE7EF),
        onSecondaryContainer = Color(0xFF061E24),
        tertiary         = Color(0xFF565D7E),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDDE1FF),
        onTertiaryContainer = Color(0xFF121A37),
        background       = Color(0xFFFBFCFD),
        onBackground     = Color(0xFF191C1D),
        surface          = Color(0xFFFBFCFD),
        onSurface        = Color(0xFF191C1D),
        surfaceVariant   = Color(0xFFDBE4E7),
        onSurfaceVariant = Color(0xFF3F484B),
        outline          = Color(0xFF70797C)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFF5890A0),
        onPrimary        = Color(0xFF003640),
        primaryContainer = Color(0xFF004E5C),
        onPrimaryContainer = Color(0xFFAAEDFF),
        secondary        = Color(0xFFB2CBD3),
        onSecondary      = Color(0xFF1D343A),
        secondaryContainer = Color(0xFF334A51),
        onSecondaryContainer = Color(0xFFCEE7EF),
        tertiary         = Color(0xFFBEC5EB),
        onTertiary       = Color(0xFF282F4D),
        tertiaryContainer = Color(0xFF3F4565),
        onTertiaryContainer = Color(0xFFDDE1FF),
        background       = Color(0xFF0D0D0D),
        onBackground     = Color(0xFFE1E3E4),
        surface          = Color(0xFF0D0D0D),
        onSurface        = Color(0xFFE1E3E4),
        surfaceVariant   = Color(0xFF3F484B),
        onSurfaceVariant = Color(0xFFBFC8CB),
        outline          = Color(0xFF899295)
    )
}

private object BrownPotScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF9C4235),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD4),
        onPrimaryContainer = Color(0xFF400100),
        secondary        = Color(0xFF775651),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDAD4),
        onSecondaryContainer = Color(0xFF2C1511),
        tertiary         = Color(0xFF705C2E),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFBDFA6),
        onTertiaryContainer = Color(0xFF251A00),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF201A19),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF201A19),
        surfaceVariant   = Color(0xFFF5DED9),
        onSurfaceVariant = Color(0xFF52443F),
        outline          = Color(0xFF84746F)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFB06050),
        onPrimary        = Color(0xFF5F140C),
        primaryContainer = Color(0xFF7D2B20),
        onPrimaryContainer = Color(0xFFFFDAD4),
        secondary        = Color(0xFFE7BDB6),
        onSecondary      = Color(0xFF442925),
        secondaryContainer = Color(0xFF5D3F3A),
        onSecondaryContainer = Color(0xFFFFDAD4),
        tertiary         = Color(0xFFDEC48C),
        onTertiary       = Color(0xFF3E2E04),
        tertiaryContainer = Color(0xFF564419),
        onTertiaryContainer = Color(0xFFFBDFA6),
        background       = Color(0xFF0D0D0D),
        onBackground     = Color(0xFFEDE0DD),
        surface          = Color(0xFF0D0D0D),
        onSurface        = Color(0xFFEDE0DD),
        surfaceVariant   = Color(0xFF52443F),
        onSurfaceVariant = Color(0xFFD7C3BE),
        outline          = Color(0xFF9F8D86)
    )
}

private object YellowDrapeScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF705D00),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFE259),
        onPrimaryContainer = Color(0xFF221B00),
        secondary        = Color(0xFF685E40),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF1E2BC),
        onSecondaryContainer = Color(0xFF221B04),
        tertiary         = Color(0xFF446650),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC6ECCE),
        onTertiaryContainer = Color(0xFF002112),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF1E1C13),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF1E1C13),
        surfaceVariant   = Color(0xFFEAE2CF),
        onSurfaceVariant = Color(0xFF4C4736),
        outline          = Color(0xFF7E7763)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFE8C500),
        onPrimary        = Color(0xFF3A3000),
        primaryContainer = Color(0xFF554600),
        onPrimaryContainer = Color(0xFFFFE259),
        secondary        = Color(0xFFD4C6A1),
        onSecondary      = Color(0xFF383017),
        secondaryContainer = Color(0xFF4F472A),
        onSecondaryContainer = Color(0xFFF1E2BC),
        tertiary         = Color(0xFFABD0B3),
        onTertiary       = Color(0xFF173724),
        tertiaryContainer = Color(0xFF2D4E39),
        onTertiaryContainer = Color(0xFFC6ECCE),
        background       = Color(0xFF1E1C13),
        onBackground     = Color(0xFFE8E2D0),
        surface          = Color(0xFF1E1C13),
        onSurface        = Color(0xFFE8E2D0),
        surfaceVariant   = Color(0xFF4C4736),
        onSurfaceVariant = Color(0xFFCEC6B4),
        outline          = Color(0xFF979180)
    )
}

private object GreenBenzaitenScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF006D40),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB2F1C1),
        onPrimaryContainer = Color(0xFF00210F),
        secondary        = Color(0xFF4F6353),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD2E8D4),
        onSecondaryContainer = Color(0xFF0D1F13),
        tertiary         = Color(0xFF3A656B),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBEEAF2),
        onTertiaryContainer = Color(0xFF001F24),
        background       = Color(0xFFFBFDF8),
        onBackground     = Color(0xFF191C19),
        surface          = Color(0xFFFBFDF8),
        onSurface        = Color(0xFF191C19),
        surfaceVariant   = Color(0xFFDDE5DB),
        onSurfaceVariant = Color(0xFF414942),
        outline          = Color(0xFF717971)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFF96D5A6),
        onPrimary        = Color(0xFF00391E),
        primaryContainer = Color(0xFF00522E),
        onPrimaryContainer = Color(0xFFB2F1C1),
        secondary        = Color(0xFFB6CCB8),
        onSecondary      = Color(0xFF223527),
        secondaryContainer = Color(0xFF384B3E),
        onSecondaryContainer = Color(0xFFD2E8D4),
        tertiary         = Color(0xFFA2CED6),
        onTertiary       = Color(0xFF01363D),
        tertiaryContainer = Color(0xFF1E4D53),
        onTertiaryContainer = Color(0xFFBEEAF2),
        background       = Color(0xFF0D0D0D),
        onBackground     = Color(0xFFE1E3DE),
        surface          = Color(0xFF0D0D0D),
        onSurface        = Color(0xFFE1E3DE),
        surfaceVariant   = Color(0xFF414942),
        onSurfaceVariant = Color(0xFFC1C9BF),
        outline          = Color(0xFF8B938A)
    )
}

private object GoldCrownScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF6E4E00),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDF99),
        onPrimaryContainer = Color(0xFF231600),
        secondary        = Color(0xFF685C40),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF0E0BB),
        onSecondaryContainer = Color(0xFF221B04),
        tertiary         = Color(0xFF496253),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFCBE8D4),
        onTertiaryContainer = Color(0xFF062015),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF1F1C14),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF1F1C14),
        surfaceVariant   = Color(0xFFECE2CF),
        onSurfaceVariant = Color(0xFF4D4736),
        outline          = Color(0xFF7F7763)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFFFBA00),
        onPrimary        = Color(0xFF3A2900),
        primaryContainer = Color(0xFF543B00),
        onPrimaryContainer = Color(0xFFFFDF99),
        secondary        = Color(0xFFD3C5A0),
        onSecondary      = Color(0xFF372F15),
        secondaryContainer = Color(0xFF4F4529),
        onSecondaryContainer = Color(0xFFF0E0BB),
        tertiary         = Color(0xFFB0CDB9),
        onTertiary       = Color(0xFF1C3527),
        tertiaryContainer = Color(0xFF324B3C),
        onTertiaryContainer = Color(0xFFCBE8D4),
        background       = Color(0xFF1F1C14),
        onBackground     = Color(0xFFEAE2D2),
        surface          = Color(0xFF1F1C14),
        onSurface        = Color(0xFFEAE2D2),
        surfaceVariant   = Color(0xFF4D4736),
        onSurfaceVariant = Color(0xFFCFC6B4),
        outline          = Color(0xFF99907C)
    )
}

private fun paletteFor(accent: VeenaAccent, dark: Boolean): ColorScheme = when (accent) {
    VeenaAccent.MATERIAL_YOU      -> if (dark) darkColorScheme() else lightColorScheme()
    VeenaAccent.PINK_LOTUS        -> if (dark) PinkLotusScheme.dark       else PinkLotusScheme.light
    VeenaAccent.BEIGE_MANUSCRIPT  -> if (dark) BeigeManuscriptScheme.dark else BeigeManuscriptScheme.light
    VeenaAccent.BLUE_WATER        -> if (dark) BlueWaterScheme.dark       else BlueWaterScheme.light
    VeenaAccent.BROWN_POT         -> if (dark) BrownPotScheme.dark        else BrownPotScheme.light
    VeenaAccent.YELLOW_DRAPE      -> if (dark) YellowDrapeScheme.dark     else YellowDrapeScheme.light
    VeenaAccent.GREEN_BENZAITEN   -> if (dark) GreenBenzaitenScheme.dark  else GreenBenzaitenScheme.light
    VeenaAccent.GOLD_CROWN        -> if (dark) GoldCrownScheme.dark       else GoldCrownScheme.light
}

@Composable
fun VeenaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    accent: VeenaAccent = VeenaAccent.MATERIAL_YOU,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = remember(darkTheme, accent, isAmoled) {
        val base = when {
            accent == VeenaAccent.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            else -> paletteFor(accent, darkTheme)
        }
        if (darkTheme && isAmoled) {
            base.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color(0xFF121212)
            )
        } else {
            base
        }
    }

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}