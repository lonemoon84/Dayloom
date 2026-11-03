package io.github.lonemoonspace.dayloom.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeavePatternTest {

    @Test
    fun `floats alternate direction like a checkerboard and stay inside their cells`() {
        val cell = 20f
        val inset = 3f
        val floats = weaveFloats(cell, inset)
        assertEquals(4, floats.size)
        val horizontal = floats.map { (a, b) -> a.y == b.y }
        // Top-left and bottom-right across, the other two down. / 左上与右下横向，另两格纵向。
        assertEquals(listOf(true, false, false, true), horizontal)
        floats.forEach { (a, b) ->
            val col = ((a.x + b.x) / 2 / cell).toInt()
            val row = ((a.y + b.y) / 2 / cell).toInt()
            for (p in listOf(a, b)) {
                assertTrue("$p left its cell", p.x in col * cell..(col + 1) * cell && p.y in row * cell..(row + 1) * cell)
            }
            // Each float stops short of the cell edges, so the crossing thread shows a gap. / 每段都不碰格子边缘，交叉的线才有间隙。
            assertEquals(cell - 2 * inset, maxOf(b.x - a.x, b.y - a.y), 1e-4f)
        }
    }
}
