package io.github.lonemoonspace.dayloom.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Card colours shared by every card, taken from the colour scheme by [appSurfacesFor].
 * 所有卡片共用的底色，由 [appSurfacesFor] 从配色中取。
 */
@Immutable
class AppSurfaces(
    val card: Color,
    /** A tile inside a card: halfway between card and surface. / 卡片里再分一层的小块：卡片色与 surface 各半。 */
    val tile: Color,
    /**
     * A hairline around cards, so their edge stays crisp where page and card tones are close. Transparent draws none.
     * 卡片外的一圈细边，页面与卡片明度接近时边缘依然清楚。透明则不画。
     */
    val cardBorder: Color = Color.Transparent,
)

/**
 * Light cards are the lightest container on a darker page; dark cards are one step above the page. Pure function, so the
 * contrast test checks exactly what the app shows.
 * 浅色时卡片用最亮的容器色、放在更暗的页面上；深色时卡片比页面亮一级。纯函数，对比度测试检查的就是 App 实际显示的颜色。
 */
fun appSurfacesFor(scheme: ColorScheme, dark: Boolean): AppSurfaces = if (dark) {
    AppSurfaces(card = scheme.surfaceContainer, tile = scheme.surfaceContainerHigh, cardBorder = scheme.outlineVariant.copy(alpha = 0.5f))
} else {
    AppSurfaces(card = scheme.surfaceContainerLowest, tile = scheme.surfaceContainerLow, cardBorder = scheme.outlineVariant.copy(alpha = 0.4f))
}

/**
 * The neutral tones [DayloomTheme] uses (M3 baseline values; a wallpaper only changes their faint hue): light cards are
 * tone 100 on tone 96 tiles, dark cards tone 12 with tone 17 tiles. Used as the preview default and by the contrast test.
 * [DayloomTheme] 用到的中性色明度（M3 基线值；壁纸只改变其中很淡的色相）：浅色卡片为明度 100、小块 96，深色卡片 12、小块 17。
 * 作为预览默认值，也供对比度测试使用。
 */
val LightAppSurfaces = AppSurfaces(card = Color(0xFFFFFFFF), tile = Color(0xFFF7F2FA))

val DarkAppSurfaces = AppSurfaces(card = Color(0xFF211F26), tile = Color(0xFF2B2930))

val LocalAppSurfaces = staticCompositionLocalOf { LightAppSurfaces }

val MaterialTheme.appSurfaces: AppSurfaces
    @Composable
    @ReadOnlyComposable
    get() = LocalAppSurfaces.current
