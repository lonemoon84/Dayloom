package io.github.lonemoonspace.dayloom.feature.weather.ui

import kotlin.math.abs
import kotlin.math.sign

/**
 * One cubic Bézier piece of a [MonotoneCurve]: two control points, then the end point.
 * [MonotoneCurve] 里的一段三次贝塞尔曲线：两个控制点，然后是终点。
 */
internal data class CurveSegment(
    val c1x: Float,
    val c1y: Float,
    val c2x: Float,
    val c2y: Float,
    val x: Float,
    val y: Float,
)

/**
 * A smooth curve through the hourly temperatures that never bends past them: monotone cubic interpolation (Steffen, 1990).
 * A point gets a flat tangent only where the temperature turns, so a steady rise is one smooth slope instead of a step at
 * every hour, and a peak never overshoots the real high.
 * 平滑穿过每小时温度、又不越过它们的曲线：单调三次插值（Steffen，1990）。只有温度拐头的点切线才是水平的，所以持续升温画成
 * 一条平滑的坡，而不是每个整点一个台阶；峰值也不会冲过实际最高温。
 */
internal object MonotoneCurve {

    /**
     * The pieces from point 0 to the last point; start the path at (`xs[0]`, `ys[0]`). [xs] must be increasing.
     * 从第 0 个点到最后一个点的各段曲线；路径从 (`xs[0]`, `ys[0]`) 开始。[xs] 必须递增。
     */
    fun segments(xs: FloatArray, ys: FloatArray): List<CurveSegment> {
        require(xs.size == ys.size) { "xs and ys differ in length" }
        val m = slopes(xs, ys)
        return (1 until xs.size).map { i ->
            // A third of the way in from each end along that end's tangent, which keeps every control point between the
            // two end heights. / 控制点沿各自端点的切线往里走三分之一，保证每个控制点都落在两端高度之间。
            val third = (xs[i] - xs[i - 1]) / 3
            CurveSegment(
                c1x = xs[i - 1] + third,
                c1y = ys[i - 1] + m[i - 1] * third,
                c2x = xs[i] - third,
                c2y = ys[i] - m[i] * third,
                x = xs[i],
                y = ys[i],
            )
        }
    }

    /** The tangent dy/dx at each point. / 每个点的切线斜率 dy/dx。 */
    fun slopes(xs: FloatArray, ys: FloatArray): FloatArray {
        val n = xs.size
        val m = FloatArray(n)
        if (n < 2) return m
        fun h(i: Int) = xs[i + 1] - xs[i]
        fun secant(i: Int) = if (h(i) > 0f) (ys[i + 1] - ys[i]) / h(i) else 0f
        if (n == 2) {
            m[0] = secant(0)
            m[1] = secant(0)
            return m
        }
        for (i in 1 until n - 1) {
            val s0 = secant(i - 1)
            val s1 = secant(i)
            val width = h(i - 1) + h(i)
            val blended = if (width > 0f) (s0 * h(i) + s1 * h(i - 1)) / width else 0f
            // Zero where the temperature turns or holds; otherwise no steeper than twice either neighbouring secant.
            // 温度拐头或不变时为 0；否则不超过两侧割线斜率的两倍。
            m[i] = (s0.sign + s1.sign) * minOf(abs(s0), abs(s1), 0.5f * abs(blended))
        }
        // The ends follow their own segment, bent towards the next tangent. / 两端顺着自己那一段，向相邻点的切线略微弯曲。
        m[0] = (3 * secant(0) - m[1]) / 2
        m[n - 1] = (3 * secant(n - 2) - m[n - 2]) / 2
        return m
    }
}
