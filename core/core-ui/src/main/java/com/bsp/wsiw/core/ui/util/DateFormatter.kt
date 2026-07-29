package com.bsp.wsiw.core.ui.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

private val INPUT_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
// Locale.getDefault() is captured at class-load time; a mid-session locale change will not
// be reflected until the process restarts. Acceptable for a movie app — users don't switch
// language while browsing cast details.
@SuppressLint("ConstantLocale")
private val OUTPUT_FORMAT = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

fun formatTmdbDate(raw: String): String = try {
    OUTPUT_FORMAT.format(INPUT_FORMAT.parse(raw)!!)
} catch (_: Exception) {
    raw
}
