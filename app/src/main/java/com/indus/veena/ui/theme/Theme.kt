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

val PinkLotus       = Color(0xFFE91E63)
val BeigeManuscript = Color(0xFFBCAAA4)
val BlueWater       = Color(0xFF2196F3)
val BrownPot        = Color(0xFF795548)
val YellowDrape     = Color(0xFFF9A825)
val GreenBenzaiten  = Color(0xFF388E3C)
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
        primary          = Color(0xFFAD1457),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3E001D),
        secondary        = Color(0xFF74565F),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E2),
        onSecondaryContainer = Color(0xFF2B151C),
        tertiary         = Color(0xFF7C5635),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDCBE),
        onTertiaryContainer = Color(0xFF2E1500),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF201A1B),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF201A1B),
        surfaceVariant   = Color(0xFFF3DDE1),
        onSurfaceVariant = Color(0xFF514347),
        outline          = Color(0xFF847377)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFFFB1C8),
        onPrimary        = Color(0xFF670033),
        primaryContainer = Color(0xFF8F004C),
        onPrimaryContainer = Color(0xFFFFD9E2),
        secondary        = Color(0xFFE3BDC5),
        onSecondary      = Color(0xFF422931),
        secondaryContainer = Color(0xFF5A3F47),
        onSecondaryContainer = Color(0xFFFFD9E2),
        tertiary         = Color(0xFFEFBD94),
        onTertiary       = Color(0xFF48290C),
        tertiaryContainer = Color(0xFF623F20),
        onTertiaryContainer = Color(0xFFFFDCBE),
        background       = Color(0xFF201A1B),
        onBackground     = Color(0xFFEBE0E1),
        surface          = Color(0xFF201A1B),
        onSurface        = Color(0xFFEBE0E1),
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
        primary          = Color(0xFF0061A4),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD1E4FF),
        onPrimaryContainer = Color(0xFF001D36),
        secondary        = Color(0xFF535F70),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD7E3F7),
        onSecondaryContainer = Color(0xFF101C2B),
        tertiary         = Color(0xFF6B5778),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF2DAFF),
        onTertiaryContainer = Color(0xFF251431),
        background       = Color(0xFFFDFCFF),
        onBackground     = Color(0xFF1A1C1E),
        surface          = Color(0xFFFDFCFF),
        onSurface        = Color(0xFF1A1C1E),
        surfaceVariant   = Color(0xFFDFE2EB),
        onSurfaceVariant = Color(0xFF43474E),
        outline          = Color(0xFF73777F)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFF9ECAFF),
        onPrimary        = Color(0xFF003258),
        primaryContainer = Color(0xFF00497D),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary        = Color(0xFFBBC7DB),
        onSecondary      = Color(0xFF253140),
        secondaryContainer = Color(0xFF3B4858),
        onSecondaryContainer = Color(0xFFD7E3F7),
        tertiary         = Color(0xFFD7BEE4),
        onTertiary       = Color(0xFF3B2948),
        tertiaryContainer = Color(0xFF523F5F),
        onTertiaryContainer = Color(0xFFF2DAFF),
        background       = Color(0xFF1A1C1E),
        onBackground     = Color(0xFFE2E2E6),
        surface          = Color(0xFF1A1C1E),
        onSurface        = Color(0xFFE2E2E6),
        surfaceVariant   = Color(0xFF43474E),
        onSurfaceVariant = Color(0xFFC3C7CF),
        outline          = Color(0xFF8D9199)
    )
}

private object BrownPotScheme {
    val light = lightColorScheme(
        primary          = Color(0xFF6F4F37),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDCC8),
        onPrimaryContainer = Color(0xFF291400),
        secondary        = Color(0xFF715B4E),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDCC8),
        onSecondaryContainer = Color(0xFF29190F),
        tertiary         = Color(0xFF5A5F2F),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDFE4A7),
        onTertiaryContainer = Color(0xFF181D00),
        background       = Color(0xFFFFFBFF),
        onBackground     = Color(0xFF201B18),
        surface          = Color(0xFFFFFBFF),
        onSurface        = Color(0xFF201B18),
        surfaceVariant   = Color(0xFFF4DED5),
        onSurfaceVariant = Color(0xFF52443D),
        outline          = Color(0xFF85736B)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFFFFB68C),
        onPrimary        = Color(0xFF3F2200),
        primaryContainer = Color(0xFF583720),
        onPrimaryContainer = Color(0xFFFFDCC8),
        secondary        = Color(0xFFE3BFB0),
        onSecondary      = Color(0xFF412D22),
        secondaryContainer = Color(0xFF594338),
        onSecondaryContainer = Color(0xFFFFDCC8),
        tertiary         = Color(0xFFC3C88D),
        onTertiary       = Color(0xFF2C3107),
        tertiaryContainer = Color(0xFF42481B),
        onTertiaryContainer = Color(0xFFDFE4A7),
        background       = Color(0xFF201B18),
        onBackground     = Color(0xFFECE0DA),
        surface          = Color(0xFF201B18),
        onSurface        = Color(0xFFECE0DA),
        surfaceVariant   = Color(0xFF52443D),
        onSurfaceVariant = Color(0xFFD7C3BA),
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
        primary          = Color(0xFF1A6B30),
        onPrimary        = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFA8F5A8),
        onPrimaryContainer = Color(0xFF002108),
        secondary        = Color(0xFF52634F),
        onSecondary      = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD5E8CE),
        onSecondaryContainer = Color(0xFF101F0F),
        tertiary         = Color(0xFF3A646B),
        onTertiary       = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBEEAF1),
        onTertiaryContainer = Color(0xFF001F23),
        background       = Color(0xFFFDFDF6),
        onBackground     = Color(0xFF1A1C19),
        surface          = Color(0xFFFDFDF6),
        onSurface        = Color(0xFF1A1C19),
        surfaceVariant   = Color(0xFFDEE4D9),
        onSurfaceVariant = Color(0xFF424940),
        outline          = Color(0xFF72796F)
    )
    val dark = darkColorScheme(
        primary          = Color(0xFF8DD98E),
        onPrimary        = Color(0xFF003912),
        primaryContainer = Color(0xFF005320),
        onPrimaryContainer = Color(0xFFA8F5A8),
        secondary        = Color(0xFFBACAB3),
        onSecondary      = Color(0xFF253423),
        secondaryContainer = Color(0xFF3B4B38),
        onSecondaryContainer = Color(0xFFD5E8CE),
        tertiary         = Color(0xFFA2CED5),
        onTertiary       = Color(0xFF02363D),
        tertiaryContainer = Color(0xFF1F4D54),
        onTertiaryContainer = Color(0xFFBEEAF1),
        background       = Color(0xFF1A1C19),
        onBackground     = Color(0xFFE2E3DC),
        surface          = Color(0xFF1A1C19),
        onSurface        = Color(0xFFE2E3DC),
        surfaceVariant   = Color(0xFF424940),
        onSurfaceVariant = Color(0xFFC2C8BD),
        outline          = Color(0xFF8C9388)
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
    accent: VeenaAccent = VeenaAccent.MATERIAL_YOU,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = remember(darkTheme, accent) {
        when {
            accent == VeenaAccent.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            else -> paletteFor(accent, darkTheme)
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