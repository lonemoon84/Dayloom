package io.github.lonemoonspace.dayloom.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Hand-picked colour schemes offered next to the wallpaper colours. Saved by [id] in `AppSettings.themePreset`, where an
 * empty or unknown id means the wallpaper. A preset with no [light] scheme is dark only: in light mode the app shows
 * [LIGHT_FALLBACK] and the settings grey the preset out.
 * 与壁纸取色并列的几套精选配色。按 [id] 存在 `AppSettings.themePreset` 里，空或不认识的 id 表示跟随壁纸。没有 [light] 配色的
 * 预设只有深色：浅色模式下 App 显示 [LIGHT_FALLBACK]，设置里把它置灰。
 */
enum class ThemePreset(val id: String, val light: ColorScheme?, val dark: ColorScheme) {
    // Glacier blue with a morning-gold accent. / 冰川蓝，配晨光金点缀。
    NORDIC_DAWN(
        id = "nordic_dawn",
        light = lightPreset(
            primary = 0xFF2360E6, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFDBEAFE, onPrimaryContainer = 0xFF1E3A8A,
            secondary = 0xFF0284C7, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFE0F2FE, onSecondaryContainer = 0xFF075985,
            tertiary = 0xFFB45309, tertiaryContainer = 0xFFFEF3C7, onTertiaryContainer = 0xFF78350F,
            surface = 0xFFF1F5F9, onSurface = 0xFF0F172A, surfaceVariant = 0xFFE2E8F0, onSurfaceVariant = 0xFF475569,
            lowest = 0xFFFFFFFF, low = 0xFFEDF2F7, container = 0xFFE8EEF5, high = 0xFFDFE7F0, highest = 0xFFD5DFEA,
            outline = 0xFF94A3B8, outlineVariant = 0xFFCBD5E1, inversePrimary = 0xFF60A5FA,
        ),
        dark = darkPreset(
            primary = 0xFF60A5FA, onPrimary = 0xFF0F172A, primaryContainer = 0xFF1D4ED8, onPrimaryContainer = 0xFFDBEAFE,
            secondary = 0xFF38BDF8, onSecondary = 0xFF0C4A6E, secondaryContainer = 0xFF0369A1, onSecondaryContainer = 0xFFE0F2FE,
            tertiary = 0xFFFBBF24, tertiaryContainer = 0xFF92400E, onTertiaryContainer = 0xFFFEF3C7,
            surface = 0xFF0B0F19, onSurface = 0xFFF8FAFC, surfaceVariant = 0xFF1E293B, onSurfaceVariant = 0xFF94A3B8,
            lowest = 0xFF080C14, low = 0xFF0D131F, container = 0xFF111827, high = 0xFF1F2937, highest = 0xFF273244,
            outline = 0xFF64748B, outlineVariant = 0xFF334155, inversePrimary = 0xFF2360E6,
        ),
    ),

    // Warm amber on paper-like linen. / 暖橙色，配纸张般的米白底。
    WARM_LINEN(
        id = "warm_linen",
        light = lightPreset(
            primary = 0xFFB93C0A, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFFFEDD5, onPrimaryContainer = 0xFF7C2D12,
            secondary = 0xFFB45309, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFFEF3C7, onSecondaryContainer = 0xFF78350F,
            tertiary = 0xFF9F1239, tertiaryContainer = 0xFFFFE4E6, onTertiaryContainer = 0xFF881337,
            surface = 0xFFFAF5EE, onSurface = 0xFF292524, surfaceVariant = 0xFFF0EAE1, onSurfaceVariant = 0xFF6B645F,
            lowest = 0xFFFFFFFF, low = 0xFFF9F3EA, container = 0xFFF4EDE3, high = 0xFFEAE2D6, highest = 0xFFE2D9CB,
            outline = 0xFFA8A29E, outlineVariant = 0xFFD6CEB5, inversePrimary = 0xFFFB923C,
        ),
        dark = darkPreset(
            primary = 0xFFFB923C, onPrimary = 0xFF431407, primaryContainer = 0xFF9A3412, onPrimaryContainer = 0xFFFFEDD5,
            secondary = 0xFFFBBF24, onSecondary = 0xFF451A03, secondaryContainer = 0xFFB45309, onSecondaryContainer = 0xFFFEF3C7,
            tertiary = 0xFFFDA4AF, tertiaryContainer = 0xFF9F1239, onTertiaryContainer = 0xFFFFE4E6,
            surface = 0xFF120E0B, onSurface = 0xFFFAF7F5, surfaceVariant = 0xFF29221C, onSurfaceVariant = 0xFFA8A29E,
            lowest = 0xFF0D0A08, low = 0xFF16110D, container = 0xFF1C1612, high = 0xFF28201A, highest = 0xFF322921,
            outline = 0xFF78716C, outlineVariant = 0xFF443D37, inversePrimary = 0xFFB93C0A,
        ),
    ),

    // Calm sage green. / 舒缓的鼠尾草绿。
    SAGE_FOREST(
        id = "sage_forest",
        light = lightPreset(
            primary = 0xFF047857, onPrimary = 0xFFFFFFFF, primaryContainer = 0xFFD1FAE5, onPrimaryContainer = 0xFF064E3B,
            secondary = 0xFF0D9488, onSecondary = 0xFFFFFFFF, secondaryContainer = 0xFFCCFBF1, onSecondaryContainer = 0xFF115E59,
            tertiary = 0xFFB45309, tertiaryContainer = 0xFFFEF3C7, onTertiaryContainer = 0xFF78350F,
            surface = 0xFFF2F8F5, onSurface = 0xFF064E3B, surfaceVariant = 0xFFE2EFE9, onSurfaceVariant = 0xFF436E62,
            lowest = 0xFFFFFFFF, low = 0xFFEDF5F0, container = 0xFFE5F1EB, high = 0xFFDBEAE2, highest = 0xFFD0E2D8,
            outline = 0xFF86A89C, outlineVariant = 0xFFBFE0D4, inversePrimary = 0xFF34D399,
        ),
        dark = darkPreset(
            primary = 0xFF34D399, onPrimary = 0xFF022C22, primaryContainer = 0xFF047857, onPrimaryContainer = 0xFFD1FAE5,
            secondary = 0xFF2DD4BF, onSecondary = 0xFF042F2E, secondaryContainer = 0xFF0F766E, onSecondaryContainer = 0xFFCCFBF1,
            tertiary = 0xFFFBBF24, tertiaryContainer = 0xFF92400E, onTertiaryContainer = 0xFFFEF3C7,
            surface = 0xFF06100C, onSurface = 0xFFF2FDF8, surfaceVariant = 0xFF162B22, onSurfaceVariant = 0xFF86A89C,
            lowest = 0xFF040A07, low = 0xFF0A1813, container = 0xFF0E1F18, high = 0xFF152C22, highest = 0xFF1D372C,
            outline = 0xFF4D7C6F, outlineVariant = 0xFF234438, inversePrimary = 0xFF047857,
        ),
    ),

    // Pure black page for OLED screens, with sky-blue and aurora-green accents. / OLED 纯黑页面，配天蓝与极光绿。
    OLED_OBSIDIAN(
        id = "oled_obsidian",
        light = null,
        dark = darkPreset(
            primary = 0xFF38BDF8, onPrimary = 0xFF082F49, primaryContainer = 0xFF0369A1, onPrimaryContainer = 0xFFE0F2FE,
            secondary = 0xFF34D399, onSecondary = 0xFF022C22, secondaryContainer = 0xFF047857, onSecondaryContainer = 0xFFD1FAE5,
            tertiary = 0xFFA78BFA, tertiaryContainer = 0xFF5B21B6, onTertiaryContainer = 0xFFEDE9FE,
            surface = 0xFF000000, onSurface = 0xFFF8FAFC, surfaceVariant = 0xFF131722, onSurfaceVariant = 0xFF94A3B8,
            lowest = 0xFF000000, low = 0xFF06080D, container = 0xFF0D111A, high = 0xFF181F2E, highest = 0xFF212A3C,
            outline = 0xFF64748B, outlineVariant = 0xFF2B3648, inversePrimary = 0xFF0369A1,
        ),
    ),

    // Deep night violet. / 深夜紫。
    AURORA_TWILIGHT(
        id = "aurora_twilight",
        light = null,
        dark = darkPreset(
            primary = 0xFFC084FC, onPrimary = 0xFF3B0764, primaryContainer = 0xFF7E22CE, onPrimaryContainer = 0xFFFAF5FF,
            secondary = 0xFF818CF8, onSecondary = 0xFF1E1B4B, secondaryContainer = 0xFF4338CA, onSecondaryContainer = 0xFFEEF2FF,
            tertiary = 0xFFF472B6, tertiaryContainer = 0xFF9D174D, onTertiaryContainer = 0xFFFCE7F3,
            surface = 0xFF05030A, onSurface = 0xFFFAF5FF, surfaceVariant = 0xFF1A1428, onSurfaceVariant = 0xFFA8A29E,
            lowest = 0xFF020104, low = 0xFF0A0713, container = 0xFF110E1C, high = 0xFF1E1730, highest = 0xFF281F3E,
            outline = 0xFF7E7496, outlineVariant = 0xFF352B4D, inversePrimary = 0xFF7E22CE,
        ),
    ),
    ;

    val darkOnly: Boolean get() = light == null

    /** The scheme shown in the given mode. / 指定模式下显示的配色。 */
    fun schemeFor(dark: Boolean): ColorScheme = if (dark) this.dark else light ?: LIGHT_FALLBACK.light!!

    companion object {
        /** What a dark-only preset shows in light mode. / 只有深色的预设在浅色模式下显示的配色。 */
        val LIGHT_FALLBACK = NORDIC_DAWN

        /** Null for the wallpaper colours (empty id) and for ids this version does not know. / 壁纸取色（空 id）与本版本不认识的 id 返回 null。 */
        fun of(id: String): ThemePreset? = entries.firstOrNull { it.id == id }
    }
}

/**
 * The page sits one step below the white cards ([lowest]) in light mode, as with the wallpaper colours, so cards stand out
 * without relying on their hairline border. Error colours are the M3 baseline ones.
 * 浅色时页面比白色卡片（[lowest]）暗一级，与壁纸取色时一样，卡片不靠细边也分得开。错误色用 M3 基线值。
 */
private fun lightPreset(
    primary: Long, onPrimary: Long, primaryContainer: Long, onPrimaryContainer: Long,
    secondary: Long, onSecondary: Long, secondaryContainer: Long, onSecondaryContainer: Long,
    tertiary: Long, tertiaryContainer: Long, onTertiaryContainer: Long,
    surface: Long, onSurface: Long, surfaceVariant: Long, onSurfaceVariant: Long,
    lowest: Long, low: Long, container: Long, high: Long, highest: Long,
    outline: Long, outlineVariant: Long, inversePrimary: Long,
): ColorScheme = lightColorScheme(
    primary = Color(primary), onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer), onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(inversePrimary),
    secondary = Color(secondary), onSecondary = Color(onSecondary),
    secondaryContainer = Color(secondaryContainer), onSecondaryContainer = Color(onSecondaryContainer),
    tertiary = Color(tertiary), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(tertiaryContainer), onTertiaryContainer = Color(onTertiaryContainer),
    background = Color(container), onBackground = Color(onSurface),
    surface = Color(surface), onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant), onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(primary),
    inverseSurface = Color(onSurface), inverseOnSurface = Color(low),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    outline = Color(outline), outlineVariant = Color(outlineVariant),
    surfaceBright = Color(lowest), surfaceDim = Color(highest),
    surfaceContainerLowest = Color(lowest), surfaceContainerLow = Color(low), surfaceContainer = Color(container),
    surfaceContainerHigh = Color(high), surfaceContainerHighest = Color(highest),
)

/**
 * The page is the darkest tone ([surface]) and cards one step lighter, as with the wallpaper colours.
 * 页面是最暗的 [surface]，卡片亮一级，与壁纸取色时一样。
 */
private fun darkPreset(
    primary: Long, onPrimary: Long, primaryContainer: Long, onPrimaryContainer: Long,
    secondary: Long, onSecondary: Long, secondaryContainer: Long, onSecondaryContainer: Long,
    tertiary: Long, tertiaryContainer: Long, onTertiaryContainer: Long,
    surface: Long, onSurface: Long, surfaceVariant: Long, onSurfaceVariant: Long,
    lowest: Long, low: Long, container: Long, high: Long, highest: Long,
    outline: Long, outlineVariant: Long, inversePrimary: Long,
): ColorScheme = darkColorScheme(
    primary = Color(primary), onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer), onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(inversePrimary),
    secondary = Color(secondary), onSecondary = Color(onSecondary),
    secondaryContainer = Color(secondaryContainer), onSecondaryContainer = Color(onSecondaryContainer),
    tertiary = Color(tertiary), onTertiary = Color(surface),
    tertiaryContainer = Color(tertiaryContainer), onTertiaryContainer = Color(onTertiaryContainer),
    background = Color(surface), onBackground = Color(onSurface),
    surface = Color(surface), onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant), onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(primary),
    inverseSurface = Color(onSurface), inverseOnSurface = Color(container),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(outline), outlineVariant = Color(outlineVariant),
    surfaceBright = Color(highest), surfaceDim = Color(surface),
    surfaceContainerLowest = Color(lowest), surfaceContainerLow = Color(low), surfaceContainer = Color(container),
    surfaceContainerHigh = Color(high), surfaceContainerHighest = Color(highest),
)
