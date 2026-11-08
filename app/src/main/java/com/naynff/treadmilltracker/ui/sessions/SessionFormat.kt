package com.naynff.treadmilltracker.ui.sessions

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LOCALE = Locale.FRENCH
private val DAY_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", LOCALE)
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", LOCALE)
private val DAY_OF_MONTH_FORMAT = DateTimeFormatter.ofPattern("d", LOCALE)
private val DAY_AND_MONTH_FORMAT = DateTimeFormatter.ofPattern("d MMM", LOCALE)
private val DAY_MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE)
private const val MINUTES_PER_HOUR = 60
private const val SECONDS_PER_MINUTE = 60

fun formatDistance(km: Double): String = String.format(LOCALE, "%.2f km", km)

/** A week's distance, where the hundredths would only be noise. */
fun formatTotalDistance(km: Double): String = String.format(LOCALE, "%.1f km", km)

fun formatSpeed(kmh: Double): String = String.format(LOCALE, "%.1f km/h", kmh)

fun formatSpeedTenths(tenths: Int): String = "${tenths / 10},${tenths % 10}"

/** "45 min" under an hour, then "1 h", "2 h 15". */
fun formatDuration(minutes: Int): String {
    if (minutes < MINUTES_PER_HOUR) return "$minutes min"
    val hours = minutes / MINUTES_PER_HOUR
    val rest = minutes % MINUTES_PER_HOUR
    return if (rest == 0) "$hours h" else String.format(LOCALE, "%d h %02d", hours, rest)
}

fun formatPace(secondsPerKm: Int): String =
    String.format(LOCALE, "%d:%02d /km", secondsPerKm / SECONDS_PER_MINUTE, secondsPerKm % SECONDS_PER_MINUTE)

fun formatCadence(stepsPerMinute: Double): String = String.format(LOCALE, "%.0f /min", stepsPerMinute)

fun formatStride(meters: Double): String = String.format(LOCALE, "%.2f m", meters)

fun formatSteps(steps: Int): String = NumberFormat.getIntegerInstance(LOCALE).format(steps)

/** "Dim. 8 nov." */
fun formatDay(date: LocalDate): String =
    DAY_FORMAT.format(date).replaceFirstChar { it.titlecase(LOCALE) }

fun formatDay(instant: Instant, zone: ZoneId): String = formatDay(instant.atZone(zone).toLocalDate())

fun formatTime(instant: Instant, zone: ZoneId): String = TIME_FORMAT.format(instant.atZone(zone))

/** "2 – 8 nov.", "26 oct. – 1 nov.", with the year when it is not [today]'s. */
fun formatWeekRange(start: LocalDate, end: LocalDate, today: LocalDate): String {
    val startFormat = if (start.month == end.month) DAY_OF_MONTH_FORMAT else DAY_AND_MONTH_FORMAT
    val endFormat = if (end.year == today.year) DAY_AND_MONTH_FORMAT else DAY_MONTH_YEAR_FORMAT
    return "${startFormat.format(start)} – ${endFormat.format(end)}"
}
