package com.bsp.wsiw.core.ui.util

import java.text.SimpleDateFormat
import java.util.Locale

private val INPUT_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val OUTPUT_FORMAT = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

fun formatTmdbDate(raw: String): String = try {
    OUTPUT_FORMAT.format(INPUT_FORMAT.parse(raw)!!)
} catch (_: Exception) {
    raw
}
