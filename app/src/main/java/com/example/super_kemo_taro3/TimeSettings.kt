package com.example.super_kemo_taro3

/** 時刻の保存キーと初期値。保存キーは従来の設定データを引き継げるように維持する。 */
data class TimeSetting(val saveKey: String, val defaultHour: Int, val defaultMinute: Int) {
    val defaultTime: String get() = formatTime(defaultHour, defaultMinute)
}

// 各時限の開始・終了を交互に並べる。偶数の添字が開始、奇数の添字が終了。
val timeSettings = listOf(
    TimeSetting("time_start_1", 8, 40), TimeSetting("time_end_1", 9, 30),
    TimeSetting("time_start_2", 9, 40), TimeSetting("time_end_2", 10, 30),
    TimeSetting("time_start_3", 10, 45), TimeSetting("time_end_3", 11, 35),
    TimeSetting("time_start_4", 11, 45), TimeSetting("time_end_4", 12, 35),
    TimeSetting("time_start_5", 13, 15), TimeSetting("time_end_5", 14, 25),
    TimeSetting("time_start_6", 14, 25), TimeSetting("time_end_6", 15, 15),
    TimeSetting("time_start_7", 15, 30), TimeSetting("time_end_7", 16, 20),
    TimeSetting("time_start_8", 16, 30), TimeSetting("time_end_8", 17, 20)
)

/** 時刻を08:05のような形式に統一する。端末の言語にかかわらず保存には半角数字を使う。 */
fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(java.util.Locale.ROOT, hour, minute)

/** HH:mm形式の時刻を0時からの経過分に変換する。不正な文字列・範囲ならnullを返す。 */
fun timeToMinutes(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

/** 不正な値、開始・終了の逆転、前の時限との重複がある入力欄の添字を返す。 */
fun invalidTimeIndices(times: List<String>): Set<Int> {
    val errors = mutableSetOf<Int>()
    var previousEnd: Int? = null
    for (index in times.indices step 2) {
        val start = timeToMinutes(times[index])
        val end = times.getOrNull(index + 1)?.let(::timeToMinutes)
        if (start == null) errors.add(index)
        if (end == null) errors.add(index + 1)
        // 終了は開始より後である必要がある。前の終了と次の開始が同時刻なら許可する。
        if (start != null && end != null && start >= end) errors.add(index + 1)
        if (start != null && previousEnd != null && previousEnd > start) errors.add(index)
        previousEnd = end
    }
    return errors
}
