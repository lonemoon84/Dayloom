package io.github.lonemoonspace.dayloom.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.lonemoonspace.dayloom.core.ui.STATUS_CHIP_TINT
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WCAG contrast of the status colours on the card, tile and chip backgrounds, and of the text pairs each theme preset
 * draws: the weather card, line tags, muted text and the greeting on the page, also where it crosses the background weave.
 * 状态色在卡片、小块与徽标底色上的 WCAG 对比度，以及每套主题预设实际用到的文字配色：天气卡、线路号、浅色文字和页面上的问候语，
 * 包括压在背景织纹线上的情况。
 */
class StatusColorsContrastTest {

    /** Every scheme the app can show, with its mode. / App 可能显示的每套配色及其模式。 */
    private val presetSchemes: List<Triple<String, ColorScheme, Boolean>> = ThemePreset.entries.flatMap { preset ->
        listOfNotNull(
            preset.light?.let { Triple("${preset.id} light", it, false) },
            Triple("${preset.id} dark", preset.dark, true),
        )
    }

    @Test
    fun `status colours reach AA text contrast on cards, tiles and chips in every scheme`() {
        val surfaces = listOf(
            Triple("wallpaper light", LightStatusColors, LightAppSurfaces),
            Triple("wallpaper dark", DarkStatusColors, DarkAppSurfaces),
        ) + presetSchemes.map { (name, scheme, dark) ->
            Triple(name, if (dark) DarkStatusColors else LightStatusColors, appSurfacesFor(scheme, dark))
        }
        for ((theme, colors, s) in surfaces) {
            for ((name, color) in colors.named()) {
                for ((bgName, bg) in listOf("card" to s.card, "tile" to s.tile)) {
                    assertTrue("$theme $name on $bgName", contrast(color, bg) >= AA_TEXT)
                    val chip = blend(color, bg, STATUS_CHIP_TINT)
                    assertTrue("$theme $name chip on $bgName", contrast(color, chip) >= AA_TEXT)
                }
            }
        }
    }

    @Test
    fun `preset text pairs reach AA contrast`() {
        for ((name, s, dark) in presetSchemes) {
            val surfaces = appSurfacesFor(s, dark)
            // The weather card: primary with white text in light mode, primary container in dark mode.
            // 天气卡：浅色时主色配白字，深色时主色容器色。
            val hero = if (dark) s.primaryContainer to s.onPrimaryContainer else s.primary to s.onPrimary
            assertTrue("$name weather card", contrast(hero.first, hero.second) >= AA_TEXT)
            assertTrue("$name line tag", contrast(s.secondaryContainer, s.onSecondaryContainer) >= AA_TEXT)
            assertTrue("$name replacement-bus tag", contrast(s.tertiaryContainer, s.onTertiaryContainer) >= AA_TEXT)
            assertTrue("$name greeting on page", contrast(s.primary, s.background) >= AA_TEXT)
            assertTrue("$name text on page", contrast(s.onBackground, s.background) >= AA_TEXT)
            for ((bgName, bg) in listOf("card" to surfaces.card, "tile" to surfaces.tile, "page" to s.background)) {
                assertTrue("$name muted text on $bgName", contrast(s.onSurfaceVariant, bg) >= AA_TEXT)
            }
            assertTrue("$name error on card", contrast(s.error, surfaces.card) >= AA_TEXT)
        }
    }

    @Test
    fun `text on the page keeps AA contrast where it crosses a weave thread`() {
        for ((name, s, dark) in presetSchemes) {
            val thread = weaveColor(s, dark)
            val woven = blend(thread, s.background, thread.alpha)
            for ((textName, text) in listOf("text" to s.onBackground, "greeting" to s.primary, "muted text" to s.onSurfaceVariant)) {
                assertTrue("$name $textName on a weave thread", contrast(text, woven) >= AA_TEXT)
            }
        }
    }

    @Test
    fun `text on see-through cards keeps AA contrast over the page and over a weave thread`() {
        // The wallpaper scheme changes with the phone; the M3 baseline stands in for it. / 壁纸配色随手机而变；用 M3 基线代替。
        val wallpaper = listOf(Triple("wallpaper light (baseline)", lightColorScheme(), false), Triple("wallpaper dark (baseline)", darkColorScheme(), true))
        for ((name, s, dark) in presetSchemes + wallpaper) {
            val solid = appSurfacesFor(s, dark)
            val clear = solid.overWeave()
            val thread = weaveColor(s, dark)
            val colors = if (dark) DarkStatusColors else LightStatusColors
            for ((underName, under) in listOf("page" to s.background, "thread" to blend(thread, s.background, thread.alpha))) {
                val card = blend(clear.card, under, clear.card.alpha)
                val tile = blend(clear.tile, card, clear.tile.alpha)
                for ((bgName, bg) in listOf("card" to card, "tile" to tile)) {
                    val where = "$name on a $bgName over the $underName"
                    assertTrue("$where: text", contrast(s.onSurface, bg) >= AA_TEXT)
                    assertTrue("$where: muted text", contrast(s.onSurfaceVariant, bg) >= AA_TEXT)
                    for ((colorName, color) in colors.named()) {
                        assertTrue("$where: $colorName", contrast(color, bg) >= AA_TEXT)
                        assertTrue("$where: $colorName chip", contrast(color, blend(color, bg, STATUS_CHIP_TINT)) >= AA_TEXT)
                    }
                }
            }
        }
    }

    @Test
    fun `cards are lighter than the page in every preset`() {
        for ((name, s, dark) in presetSchemes) {
            assertTrue(name, luminance(appSurfacesFor(s, dark).card) > luminance(s.background))
        }
    }

    @Test
    fun `dark-only presets fall back to a light preset and ids round-trip`() {
        for (preset in ThemePreset.entries) {
            assertSame(preset, ThemePreset.of(preset.id))
            if (preset.darkOnly) assertSame(ThemePreset.LIGHT_FALLBACK.light, preset.schemeFor(dark = false))
        }
        assertNotNull(ThemePreset.LIGHT_FALLBACK.light)
        assertNull(ThemePreset.of(""))
        assertNull(ThemePreset.of("removed_in_a_later_version"))
    }

    @Test
    fun `the contrast formula matches known WCAG values`() {
        assertEquals(21.0, contrast(Color.Black, Color.White), 0.001)
        assertEquals(4.54, contrast(Color(0xFF767676), Color.White), 0.01)
    }

    private fun StatusColors.named() =
        listOf("green" to green, "amber" to amber, "red" to red, "gray" to gray, "orange" to orange, "rain" to rain)

    private fun blend(fg: Color, bg: Color, alpha: Float) = Color(
        red = fg.red * alpha + bg.red * (1 - alpha),
        green = fg.green * alpha + bg.green * (1 - alpha),
        blue = fg.blue * alpha + bg.blue * (1 - alpha),
    )

    private fun luminance(c: Color): Double {
        fun lin(v: Float): Double {
            val s = Math.round(v * 255f) / 255.0
            return if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * lin(c.red) + 0.7152 * lin(c.green) + 0.0722 * lin(c.blue)
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private companion object {
        const val AA_TEXT = 4.5
    }
}
