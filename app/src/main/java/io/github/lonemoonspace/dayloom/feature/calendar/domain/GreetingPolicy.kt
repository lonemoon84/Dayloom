package io.github.lonemoonspace.dayloom.feature.calendar.domain

import java.time.LocalTime

/** The part of the day the header greets. / 页头问候的时段。 */
enum class Greeting { MORNING, AFTERNOON, EVENING }

object GreetingPolicy {
    /**
     * Morning from 05:00, afternoon from 12:00, evening from 18:00 until 05:00, so a late-night glance still says evening.
     * 05:00 起为早上，12:00 起为下午，18:00 起到次日 05:00 为晚上，深夜看一眼仍是「晚上好」。
     */
    fun at(now: LocalTime): Greeting = when (now.hour) {
        in 5..11 -> Greeting.MORNING
        in 12..17 -> Greeting.AFTERNOON
        else -> Greeting.EVENING
    }
}
