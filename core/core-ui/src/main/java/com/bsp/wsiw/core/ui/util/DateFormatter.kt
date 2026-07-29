package com.bsp.wsiw.core.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy")

fun formatTmdbDate(raw: String): String = try {
    LocalDate.parse(raw).format(DISPLAY_FORMAT)
} catch (_: Exception) {
    raw
}
