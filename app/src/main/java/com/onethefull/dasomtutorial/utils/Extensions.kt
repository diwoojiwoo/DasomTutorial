package com.onethefull.dasomtutorial.utils

/**
 * Created by sjw on 2025. 7. 28.
 */

import com.onethefull.dasomtutorial.App
import java.text.SimpleDateFormat
import java.util.*

fun Long.toKoreanTimeString(): String {
    val locale =  App.instance.getLocale() ?: Locale.KOREA
    val pattern = if (locale.language == Locale.KOREA.language || locale.language == "ko") {
        "a h:mm"  // 오전/오후 h:mm (한국어)
    } else {
        "h:mm a"  // 1:30 PM (영어권)
    }
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(Date(this))
}

fun String.isValidTimeInput(): Boolean {
    val trimmed = this.trim()

    val patterns = listOf(
        // 오전 9:30, 오후 11:15
        Regex("^(오전|오후)\\s?([1-9]|1[0-2]):[0-5][0-9]\$"),
        // 오전 9시, 오후 11시
        Regex("^(오전|오후)\\s?([1-9]|1[0-2])시\$"),
        // 오전 9시 30분, 오후 11시 15분
        Regex("^(오전|오후)\\s?([1-9]|1[0-2])시\\s?[0-5]?[0-9]분\$"),
        // 24시간제: 09:30, 23:15
        Regex("^([0-1]?\\d|2[0-3]):[0-5][0-9]\$"),
        // 4자리 숫자: 0930, 2315
        Regex("^[0-2]\\d[0-5]\\d\$"),
        // 11시, 9시
        Regex("^([0-1]?\\d|2[0-3])시\$"),
        // 11시 30분, 9시 5분
        Regex("^([0-1]?\\d|2[0-3])시\\s?[0-5]?[0-9]분\$"),
        // 오전 9 (시 생략)
        Regex("^(오전|오후)\\s?([1-9]|1[0-2])\$")
    )

    return patterns.any { it.matches(trimmed) }
}


fun parseTimeStringToMillis(timeString: String): Long? {
    val input = timeString.trim()
    val now = Calendar.getInstance()
    val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(now.time)

    val normalizedTime: String = when {
        // 오전/오후 9:30
        Regex("^(오전|오후)\\s?([1-9]|1[0-2]):([0-5][0-9])$").find(input) != null -> input

        // 오전/오후 9시
        Regex("^(오전|오후)\\s?([1-9]|1[0-2])시$").find(input)?.let {
            "${it.groupValues[1]} ${it.groupValues[2]}:00"
        } != null -> Regex("^(오전|오후)\\s?([1-9]|1[0-2])시$").replace(input, "$1 $2:00")

        // 오전/오후 9시 30분
        Regex("^(오전|오후)\\s?([1-9]|1[0-2])시\\s?([0-5]?[0-9])분$").find(input)?.let {
            "${it.groupValues[1]} ${it.groupValues[2]}:${it.groupValues[3].padStart(2, '0')}"
        } != null -> Regex("^(오전|오후)\\s?([1-9]|1[0-2])시\\s?([0-5]?[0-9])분$").replace(input) {
            "${it.groupValues[1]} ${it.groupValues[2]}:${it.groupValues[3].padStart(2, '0')}"
        }

        // 24시간제
        Regex("^([0-1]?\\d|2[0-3]):[0-5][0-9]$").matches(input) -> input

        // 4자리 숫자 (0930)
        Regex("^[0-2]\\d[0-5]\\d$").matches(input) -> {
            "${input.substring(0, 2)}:${input.substring(2, 4)}"
        }

        // 9시
        Regex("^([0-1]?\\d|2[0-3])시$").find(input)?.let {
            "${it.groupValues[1]}:00"
        } != null -> Regex("^([0-1]?\\d|2[0-3])시$").replace(input, "$1:00")

        // 9시 5분
        Regex("^([0-1]?\\d|2[0-3])시\\s?([0-5]?[0-9])분$").find(input)?.let {
            "${it.groupValues[1]}:${it.groupValues[2].padStart(2, '0')}"
        } != null -> Regex("^([0-1]?\\d|2[0-3])시\\s?([0-5]?[0-9])분$").replace(input) {
            "${it.groupValues[1]}:${it.groupValues[2].padStart(2, '0')}"
        }

        // 오전 9
        Regex("^(오전|오후)\\s?([1-9]|1[0-2])$").find(input)?.let {
            "${it.groupValues[1]} ${it.groupValues[2]}:00"
        } != null -> Regex("^(오전|오후)\\s?([1-9]|1[0-2])$").replace(input, "$1 $2:00")

        else -> return null
    }

    val format = if (normalizedTime.startsWith("오전") || normalizedTime.startsWith("오후")) {
        SimpleDateFormat("yyyy-MM-dd a h:mm", Locale.KOREA)
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
    }

    return try {
        format.parse("$todayPrefix $normalizedTime")?.time
    } catch (e: Exception) {
        null
    }
}

/**
 * 나라별 코드를 TimeZone 맵핑
 * ko-KR → Asia/Seoul → +09:00
 *
 * ja-JP → Asia/Tokyo → +09:00
 *
 * zh-CN → Asia/Shanghai → +08:00
 *
 * zh-TW → Asia/Taipei → +08:00
 *
 * en-US → America/New_York → -04:00 (서머타임 반영 시)
 * */
fun getTimeZoneFromDasomLanguageCode(code: String): TimeZone {
    return when (code) {
        "ko-KR" -> TimeZone.getTimeZone("Asia/Seoul")
        "ja-JP" -> TimeZone.getTimeZone("Asia/Tokyo")
        "zh-CN" -> TimeZone.getTimeZone("Asia/Shanghai")
        "zh-TW" -> TimeZone.getTimeZone("Asia/Taipei")
        "en-US" -> TimeZone.getTimeZone("America/New_York")
        else -> TimeZone.getTimeZone("UTC")
    }
}

fun getUtcInfoFromDasomLanguageCode(code: String): String {
    val tz = getTimeZoneFromDasomLanguageCode(code)
    val now = Date()
    val offsetInMillis = tz.rawOffset + if (tz.inDaylightTime(now)) tz.dstSavings else 0
    val hours = offsetInMillis / 3600000
    val minutes = (offsetInMillis % 3600000) / 60000
    val sign = if (hours >= 0) "+" else "-"
    return String.format("%s%02d:%02d", sign, Math.abs(hours), Math.abs(minutes))
}
