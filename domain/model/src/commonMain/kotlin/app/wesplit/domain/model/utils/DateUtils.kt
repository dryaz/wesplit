package app.wesplit.domain.model.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.math.max

/**
 * Calculates the number of days between today and a future Instant.
 *
 * @param futureInstant The future Instant to compare with today's date.
 * @return The number of days between today and the future date. Returns 0 if the future date is today or in the past.
 */
fun calculateDaysUntil(futureInstant: Instant): Int {
    // Define the desired time zone. You can change it to TimeZone.UTC or any specific TimeZone.
    val timeZone = TimeZone.currentSystemDefault()

    // Get today's date in the specified time zone.
    val today: LocalDate = Clock.System.todayIn(timeZone)

    // Convert the future Instant to LocalDate in the specified time zone.
    val futureDate: LocalDate = futureInstant.toLocalDateTime(timeZone).date

    // Calculate the number of days until the future date.
    val daysUntil: Int = today.daysUntil(futureDate)

    // Ensure that the number of days is not negative.
    return max(daysUntil, 0)
}

/**
 * Creates a validation string indicating how many days are left until the subscription expires.
 *
 * @param futureInstant The future Instant representing the expiration date.
 * @return A string in the format "Valid for X days".
 */
fun createValidityDaysString(futureInstant: Instant?): String {
    if (futureInstant == null) return "Valid Forever"

    val days = calculateDaysUntil(futureInstant)
    return "Valid for $days day${if (days != 1) "s" else ""} more"
}
