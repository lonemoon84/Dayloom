package io.github.lonemoonspace.dayloom.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A faint plain-weave texture over the page colour, echoing the app's name: every thread goes over one crossing and under
 * the next. It is one small tile repeated by a shader, built once per size and colour. Cards over it are see-through
 * ([overWeave]), so it shows behind them as well as in the gaps, the calendar header and the blurred bars.
 * 页面底色上一层淡淡的平纹织纹，呼应「织日」：每根线一上一下交织。用着色器重复一小块图案，每种尺寸和颜色只生成一次。上面的卡片
 * 是半透明的（[overWeave]），所以卡片后面、卡片间的空隙、日历页头和毛玻璃顶栏底栏都能看到。
 */
fun Modifier.weaveBackground(color: Color): Modifier = drawWithCache {
    val cell = WEAVE_CELL.toPx().roundToInt().coerceAtLeast(MIN_CELL_PX)
    val tile = ImageBitmap(2 * cell, 2 * cell)
    val paint = Paint().apply {
        this.color = color
        strokeWidth = WEAVE_STROKE.toPx()
        strokeCap = StrokeCap.Round
        isAntiAlias = true
    }
    val canvas = Canvas(tile)
    weaveFloats(cell.toFloat(), WEAVE_INSET.toPx()).forEach { (start, end) -> canvas.drawLine(start, end, paint) }
    val brush = ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    onDrawBehind { drawRect(brush) }
}

/**
 * The weave's thread colour. Light schemes use lines lighter than the page, like the tone-on-tone figure of a damask, so
 * dark text written straight on the page only gains contrast; dark schemes use a faint line of the text colour.
 * 织纹线的颜色。浅色配色用比页面更浅的线，像提花布的暗纹，直接写在页面上的深色文字对比度只会更高；深色配色用很淡的文字色线。
 */
fun weaveColor(scheme: ColorScheme, dark: Boolean): Color =
    if (dark) scheme.onBackground.copy(alpha = WEAVE_DARK_ALPHA) else Color.White.copy(alpha = WEAVE_LIGHT_ALPHA)

/**
 * The four thread floats of one 2×2 tile: across in the top-left and bottom-right cells, down in the other two, each pulled
 * in by [inset] from its cell's edges so the crossing threads look like they pass underneath.
 * 一块 2×2 图案里的四段浮线：左上和右下格横向，另两格纵向；每段从格子边缘各收进 [inset]，看起来像交叉的线从下面穿过。
 */
internal fun weaveFloats(cell: Float, inset: Float): List<Pair<Offset, Offset>> {
    val mid = cell / 2
    return listOf(
        Offset(inset, mid) to Offset(cell - inset, mid),
        Offset(cell + mid, inset) to Offset(cell + mid, cell - inset),
        Offset(mid, cell + inset) to Offset(mid, 2 * cell - inset),
        Offset(cell + inset, cell + mid) to Offset(2 * cell - inset, cell + mid),
    )
}

private val WEAVE_CELL = 5.dp
private val WEAVE_STROKE = 1.3.dp
private val WEAVE_INSET = 0.9.dp
private const val MIN_CELL_PX = 4

/** Visible on every light preset page without being noticed first. / 在每套浅色预设的页面上看得见，但不会第一眼就注意到。 */
private const val WEAVE_LIGHT_ALPHA = 0.75f

/** Keeps light text on dark pages above 6:1 even where it crosses a thread. / 浅色文字压在线上时，对比度仍在 6:1 以上。 */
private const val WEAVE_DARK_ALPHA = 0.08f
