package com.bsp.wsiw.core.ui.util

private val MONTHS = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

fun formatTmdbDate(raw: String): String {
    val parts = raw.split("-")
    if (parts.size != 3) return raw
    val year = parts[0]
    val monthIndex = parts[1].toIntOrNull()?.minus(1) ?: return raw
    val day = parts[2].toIntOrNull() ?: return raw
    val month = MONTHS.getOrNull(monthIndex) ?: return raw
    return "$month $day, $year"
}
