package com.naynff.treadmilltracker.ui

import com.naynff.treadmilltracker.ui.sessions.formatDay
import com.naynff.treadmilltracker.ui.sessions.formatDistance
import com.naynff.treadmilltracker.ui.sessions.formatDuration
import com.naynff.treadmilltracker.ui.sessions.formatPace
import com.naynff.treadmilltracker.ui.sessions.formatWeekRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

private val TODAY = LocalDate.of(2026, 11, 8)

class SessionFormatTest {
    @Test
    fun `durations switch to hours from 60 minutes`() {
        assertEquals("45 min", formatDuration(45))
        assertEquals("1 h", formatDuration(60))
        assertEquals("1 h 05", formatDuration(65))
        assertEquals("2 h 15", formatDuration(135))
    }

    @Test
    fun `numbers use the French decimal comma`() {
        assertEquals("4,88 km", formatDistance(4.875))
        assertEquals("9:14 /km", formatPace(554))
    }

    @Test
    fun `days are short and capitalized`() {
        assertEquals("Dim. 8 nov.", formatDay(TODAY))
    }

    @Test
    fun `week ranges name the month once, and the year only when it is not this one`() {
        assertEquals("2 – 8 nov.", formatWeekRange(LocalDate.of(2026, 11, 2), TODAY, TODAY))
        assertEquals(
            "26 oct. – 1 nov.",
            formatWeekRange(LocalDate.of(2026, 10, 26), LocalDate.of(2026, 11, 1), TODAY),
        )
        assertEquals(
            "29 déc. – 4 janv. 2026",
            formatWeekRange(LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 4), LocalDate.of(2027, 1, 1)),
        )
    }
}
