package com.onethefull.dasomtutorial.utils

/**
 * Created by sjw on 2025. 7. 28.
 */

import java.text.SimpleDateFormat
import java.util.*

fun Long.toKoreanTimeString(): String {
    val formatter = SimpleDateFormat("a h:mm", Locale.KOREA)
    return formatter.format(Date(this))
}