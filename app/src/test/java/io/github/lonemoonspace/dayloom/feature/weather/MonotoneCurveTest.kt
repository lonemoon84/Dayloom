package io.github.lonemoonspace.dayloom.feature.weather

import io.github.lonemoonspace.dayloom.feature.weather.ui.CurveSegment
import io.github.lonemoonspace.dayloom.feature.weather.ui.MonotoneCurve
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonotoneCurveTest {

    private fun hours(n: Int) = FloatArray(n) { it * 20f }

    /** Every control point lies between its segment's two end heights. / 每个控制点都在所在段两端高度之间。 */
    private fun assertNoOvershoot(ys: FloatArray, segments: List<CurveSegment>) {
        segments.forEachIndexed { i, s ->
            val lo = minOf(ys[i], ys[i + 1]) - EPS
            val hi = maxOf(ys[i], ys[i + 1]) + EPS
            assertTrue("segment $i c1 ${s.c1y} outside $lo..$hi", s.c1y in lo..hi)
            assertTrue("segment $i c2 ${s.c2y} outside $lo..$hi", s.c2y in lo..hi)
        }
    }

    @Test
    fun `a steady rise has no flat spot at any hour`() {
        val ys = floatArrayOf(0.0f, 0.7f, 2.2f, 4.2f, 6.0f, 7.2f, 7.9f)
        val slopes = MonotoneCurve.slopes(hours(ys.size), ys)
        slopes.forEachIndexed { i, m -> assertTrue("hour $i slope $m", m > 0f) }
        assertNoOvershoot(ys, MonotoneCurve.segments(hours(ys.size), ys))
    }

    @Test
    fun `turning points are flat and never overshoot`() {
        val ys = floatArrayOf(1f, 3.8f, 6f, 4.6f, 3f, 3.4f, 4.6f)
        val slopes = MonotoneCurve.slopes(hours(ys.size), ys)
        assertEquals(0f, slopes[2], 0f)
        assertEquals(0f, slopes[4], 0f)
        assertNoOvershoot(ys, MonotoneCurve.segments(hours(ys.size), ys))
    }

    @Test
    fun `an unchanged temperature stays flat`() {
        val ys = floatArrayOf(5f, 4.2f, 4.2f, 3.6f)
        val flat = MonotoneCurve.segments(hours(ys.size), ys)[1]
        assertEquals(4.2f, flat.c1y, 0f)
        assertEquals(4.2f, flat.c2y, 0f)
    }

    @Test
    fun `segments end on each hour and keep their control points inside it`() {
        val xs = floatArrayOf(10f, 40f, 70f, 160f)
        val ys = floatArrayOf(3f, 5f, 4f, 6f)
        val segments = MonotoneCurve.segments(xs, ys)
        assertEquals(3, segments.size)
        segments.forEachIndexed { i, s ->
            assertEquals(xs[i + 1], s.x, 0f)
            assertEquals(ys[i + 1], s.y, 0f)
            assertTrue(s.c1x > xs[i] && s.c1x < s.c2x && s.c2x < xs[i + 1])
        }
    }

    @Test
    fun `two points make a straight line and fewer make nothing`() {
        val s = MonotoneCurve.segments(floatArrayOf(0f, 30f), floatArrayOf(2f, 8f)).single()
        assertEquals(4f, s.c1y, EPS)
        assertEquals(6f, s.c2y, EPS)
        assertTrue(MonotoneCurve.segments(floatArrayOf(0f), floatArrayOf(2f)).isEmpty())
        assertTrue(MonotoneCurve.segments(floatArrayOf(), floatArrayOf()).isEmpty())
    }

    @Test
    fun `random days never overshoot`() {
        val random = Random(42)
        repeat(500) {
            val n = random.nextInt(3, 30)
            val ys = FloatArray(n) { random.nextInt(-80, 120) / 10f }
            assertNoOvershoot(ys, MonotoneCurve.segments(hours(n), ys))
        }
    }

    private companion object {
        const val EPS = 1e-4f
    }
}
