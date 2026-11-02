package io.github.lonemoonspace.dayloom.feature.calendar

import io.github.lonemoonspace.dayloom.feature.calendar.domain.Greeting
import io.github.lonemoonspace.dayloom.feature.calendar.domain.GreetingPolicy
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingPolicyTest {

    private fun at(h: Int, m: Int) = GreetingPolicy.at(LocalTime.of(h, m))

    @Test
    fun `the greeting changes at 05, 12 and 18 o'clock`() {
        assertEquals(Greeting.EVENING, at(4, 59))
        assertEquals(Greeting.MORNING, at(5, 0))
        assertEquals(Greeting.MORNING, at(11, 59))
        assertEquals(Greeting.AFTERNOON, at(12, 0))
        assertEquals(Greeting.AFTERNOON, at(17, 59))
        assertEquals(Greeting.EVENING, at(18, 0))
        assertEquals(Greeting.EVENING, at(0, 0))
    }
}
