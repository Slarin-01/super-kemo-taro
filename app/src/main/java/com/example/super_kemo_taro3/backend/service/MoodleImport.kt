/*
 * バックエンド・処理: 岡大Moodleの履修科目と公開シラバスから、ミュートする曜日・時限を決める。
 * 通信の実装は受け取った本文の解釈と分離し、本文の解釈は端末APIなしで検証する。
 */
package com.example.super_kemo_taro3.backend.service

import java.net.URLDecoder
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Calendar
import java.util.TimeZone

// 1. 接続先と、アプリへ戻ったトークン
/** 岡大Moodleのモバイル起動と、koto-bellスキームで戻るトークンを扱う。 */
object MoodleImport {
    const val ORIGIN = "https://moodle.el.okayama-u.ac.jp"
    const val SCHEME = "koto-bell"
    const val CALENDAR_URL = "https://www.okayama-u.ac.jp/tp/student/kyomu1_2_1.html"
    const val SEARCH_URL = "https://kyomu.adm.okayama-u.ac.jp/Portal/Public/Syllabus/SearchMain.aspx"
    private const val REST = "$ORIGIN/webservice/rest/server.php"
    private val hex32 = Regex("[a-f0-9]{32}")
    private val yearField = "ctl00${"$"}phContents${"$"}ddl_year"
    private val codeField = "ctl00${"$"}phContents${"$"}txt_lctCd_Search"
    private val searchButton = "ctl00${"$"}phContents${"$"}ctl18${"$"}btnSearch"
    private val weekdays = "月火水木金土日"
    private val dayKeys = listOf("MON", "TUE", "WED", "THU", "FRI")
    private val slotPattern = Regex("(?<!\\d)([月火水木金土日])曜?(\\d+)(?:[〜～~\\-](\\d+))?")

    fun launchUrl(passport: String): String {
        val query = "service=moodle_mobile_app&passport=$passport&urlscheme=$SCHEME&confirmed=1"
        return "$ORIGIN/admin/tool/mobile/launch.php?$query"
    }

    /** Moodleが返す siteId は、起動時に渡した passport とサイトURLのMD5。 */
    fun passportSiteId(passport: String): String {
        val digest = MessageDigest.getInstance("MD5").digest((ORIGIN + passport).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    fun returnedToken(url: String): ReturnedToken? {
        if (url.length > 8192) return null
        val matched = Regex("^${Regex.escape(SCHEME)}://token=([^&#]+)$").matchEntire(url) ?: return null
        val decoded = try {
            val encoded = matched.groupValues[1].replace("+", "%2B")
            String(decodeBase64(URLDecoder.decode(encoded, "UTF-8")), Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            return null
        }
        val parts = decoded.split(":::")
        if (parts.size !in 2..3 || !hex32.matches(parts[0]) || !hex32.matches(parts[1])) return null
        return ReturnedToken(parts[0], parts[1])
    }

    fun tokyoToday(nowMillis: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"))
        calendar.timeInMillis = nowMillis
        return "%04d-%02d-%02d".format(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // 2. 履修科目の講義番号
    /** 終了済みを除き、idnumberまたは科目名先頭の10桁を講義番号にする。 */
    fun lectureCodes(json: String, nowEpoch: Long): List<String> {
        val codes = mutableListOf<String>()
        for (course in jsonObjects(json)) {
            val fields = topFields(course)
            val end = fields["enddate"]?.toLongOrNull()
            if (end != null && end != 0L && end <= nowEpoch) continue
            val idnumber = fields["idnumber"]
            val code = if (idnumber != null && Regex("\\d{10}").matches(idnumber)) {
                idnumber
            } else {
                Regex("\\[(\\d{10})]").find(fields["fullname"] ?: "")?.groupValues?.get(1)
            }
            if (code != null) codes += code
        }
        return codes.distinct()
    }

    fun userId(json: String): String {
        val id = topFields(json.trim().removePrefix("\uFEFF"))["userid"]
        if (id == null || !Regex("\\d+").matches(id)) throw ImportFailed("Moodleを取得できません")
        return id
    }

    fun moodleFailure(json: String): String? {
        val trimmed = json.trim().removePrefix("\uFEFF")
        if (!trimmed.startsWith("{")) return null
        val fields = topFields(trimmed)
        if ("exception" !in fields) return null
        return fields["message"]?.takeIf { it.isNotBlank() } ?: "Moodleを取得できません"
    }

    // 3. 公開シラバスと学年暦
    fun parseForm(page: String): Map<String, String> {
        val data = linkedMapOf<String, String>()
        var select: String? = null
        for (token in tokens(page)) {
            if (token !is Markup.Tag) continue
            when (token.name) {
                "input" -> {
                    if (token.close) continue
                    val name = token.attrs["name"] ?: continue
                    val kind = token.attrs["type"] ?: "text"
                    if (kind == "submit" || kind == "image") continue
                    if ((kind == "checkbox" || kind == "radio") && "checked" !in token.attrs) continue
                    data[name] = token.attrs["value"] ?: ""
                }
                "select" -> if (!token.close) {
                    select = token.attrs["name"]
                    if (select != null) data.putIfAbsent(select, "")
                } else {
                    select = null
                }
                "option" -> if (select != null && "selected" in token.attrs) {
                    data[select] = token.attrs["value"] ?: ""
                }
            }
        }
        return data
    }

    fun parseSyllabus(page: String): List<SyllabusCourse> {
        val found = mutableListOf<SyllabusCourse>()
        var inTable = false
        var inRow = false
        var inCell = false
        val text = StringBuilder()
        var href: String? = null
        var row = mutableListOf<Pair<String, String?>>()
        fun finishCell() {
            if (!inCell) return
            inCell = false
            row += text.toString().trim() to href
        }
        for (token in tokens(page)) {
            when (token) {
                is Markup.Tag -> when (token.name) {
                    "table" -> if (!token.close && token.attrs["id"] == "ctl00_phContents_ucGrid_grv") {
                        inTable = true
                    } else if (token.close && inTable && !inRow) {
                        inTable = false
                    }
                    "tr" -> if (inTable && !token.close) {
                        inRow = true
                        row = mutableListOf()
                    } else if (token.close && inRow) {
                        finishCell()
                        inRow = false
                        courseFrom(row)?.takeIf { row.isNotEmpty() && row[0].first.all { it.isDigit() } }?.let { found += it }
                    }
                    "td" -> if (inRow && !token.close) {
                        inCell = true
                        text.clear()
                        href = null
                    } else if (token.close) {
                        finishCell()
                    }
                    "a" -> if (inCell && href == null) href = token.attrs["href"]
                }
                is Markup.Text -> if (inCell) text.append(token.value)
            }
        }
        return found
    }

    fun lookupLecture(code: String, pages: PageFetch): SyllabusCourse? {
        if (!Regex("[0-9]{10}").matches(code)) throw ImportFailed("講義番号が不正です")
        val form = parseForm(pages.load(SEARCH_URL, null)).toMutableMap()
        form[yearField] = code.take(4)
        form[codeField] = code
        form[searchButton] = "検 索"
        return parseSyllabus(pages.load(SEARCH_URL, form)).firstOrNull { it.code == code }
    }

    fun parseCalendar(page: String): Map<String, Map<String, DateSpan>> {
        val section = Regex("<h2\\b[^>]*>\\s*学期\\s*</h2>(.*?)(?=<h2\\b|$)", RegexOption.DOT_MATCHES_ALL)
            .find(page) ?: throw ImportFailed("学期の学年暦がありません")
        val calendars = mutableMapOf<String, Map<String, DateSpan>>()
        val blocks = Regex("<h3\\b[^>]*>(.*?)</h3>(.*?)(?=<h3\\b|$)", RegexOption.DOT_MATCHES_ALL)
            .findAll(section.groupValues[1])
        for (block in blocks) {
            val year = Regex("(20\\d{2})年度").find(plain(block.groupValues[1]))?.groupValues?.get(1) ?: continue
            val periods = mutableMapOf<String, DateSpan>()
            for (row in Regex("<tr\\b[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL).findAll(block.groupValues[2])) {
                val cells = Regex("<t[hd]\\b[^>]*>(.*?)</t[hd]>", RegexOption.DOT_MATCHES_ALL)
                    .findAll(row.groupValues[1]).map { plain(it.groupValues[1]) }.toList()
                for (index in cells.indices step 2) {
                    if (index + 1 >= cells.size) continue
                    val label = cells[index]
                    if (!Regex("第[1-4]学期|前期|後期").matches(label)) continue
                    val match = Regex("(\\d{1,2})月(\\d{1,2})日[~〜](\\d{1,2})月(\\d{1,2})日").matchEntire(cells[index + 1])
                        ?: continue
                    val startMonth = match.groupValues[1].toInt()
                    val endMonth = match.groupValues[3].toInt()
                    val start = isoDate(year.toInt(), startMonth, match.groupValues[2].toInt())
                    val end = isoDate(year.toInt(), endMonth, match.groupValues[4].toInt())
                    if (start > end) throw ImportFailed("学年暦の日付が不正です")
                    periods[label] = DateSpan(start, end)
                }
            }
            if ((1..4).all { "第${it}学期" in periods }) calendars[year] = periods
        }
        if (calendars.isEmpty()) throw ImportFailed("学年暦の学期を読み取れません")
        return calendars
    }

    // 4. 今日開講している科目を、月〜金の1〜8限へ置く
    fun academicYear(day: String): Int {
        val year = day.substring(0, 4).toInt()
        return if (day.substring(5, 7).toInt() < 4) year - 1 else year
    }

    fun termDates(year: Int, term: String?, calendars: Map<String, Map<String, DateSpan>>): List<DateSpan>? {
        if (term.isNullOrBlank()) return null
        val label = Normalizer.normalize(term, Normalizer.Form.NFKC).trim()
        if (label == "通年") return listOf(DateSpan("$year-04-01", "${year + 1}-03-31"))
        val labels = when {
            label == "前期" || label == "後期" -> listOf(label)
            "学期" in label -> {
                val numbers = Regex("[1-4]").findAll(label).map { it.value.toInt() }.toMutableSet()
                Regex("([1-4])[~〜-](?:第)?([1-4])").findAll(label).forEach { match ->
                    val first = match.groupValues[1].toInt()
                    val last = match.groupValues[2].toInt()
                    if (first <= last) numbers += first..last
                }
                numbers.sorted().map { "第${it}学期" }
            }
            else -> return null
        }
        if (labels.isEmpty()) return null
        val calendar = calendars[year.toString()] ?: return null
        if (labels.any { it !in calendar }) return null
        return labels.map { calendar.getValue(it) }
    }

    fun courseActive(course: SyllabusCourse, calendars: Map<String, Map<String, DateSpan>>, today: String): Boolean {
        val year = course.code.take(4).toIntOrNull() ?: return false
        if (year != academicYear(today)) return false
        val spans = termDates(year, course.term, calendars) ?: return false
        return spans.any { it.start <= today && today <= it.end }
    }

    /** 月〜金・1〜8限に入る枠だけを保存キーにする。1セル内の複数曜限はすべて使う。 */
    fun muteKeys(course: SyllabusCourse): Set<String> {
        val keys = mutableSetOf<String>()
        // 10月1日のような日付は曜限ではない。土日と9限以降は40枠の外。
        for (match in slotPattern.findAll(course.slotText)) {
            val weekday = weekdays.indexOf(match.groupValues[1])
            val period = match.groupValues[2].toInt()
            val end = match.groupValues[3].toIntOrNull() ?: period
            if (weekday !in dayKeys.indices || end < period) continue
            for (lesson in period..end) if (lesson in 1..8) keys += "${dayKeys[weekday]}_$lesson"
        }
        return keys
    }

    // 5. 取得済み本文から、今日のミュート枠を集める
    fun collect(
        token: String,
        today: String,
        nowEpoch: Long,
        pages: PageFetch,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportResult {
        val site = moodle(token, "core_webservice_get_site_info", emptyMap(), pages)
        val courses = moodle(token, "core_enrol_get_users_courses", mapOf("userid" to userId(site)), pages)
        val codes = lectureCodes(courses, nowEpoch)
        if (codes.isEmpty()) throw ImportFailed("履修中の科目を読み取れません")
        onProgress(0, codes.size)
        val calendars = parseCalendar(pages.load(CALENDAR_URL, null))
        val keys = mutableSetOf<String>()
        var unmatched = 0
        for ((index, code) in codes.withIndex()) {
            val course = lookupLecture(code, pages)
            val slots = if (course != null && courseActive(course, calendars, today)) muteKeys(course) else emptySet()
            if (slots.isEmpty()) unmatched += 1 else keys += slots
            onProgress(index + 1, codes.size)
        }
        return ImportResult(keys, unmatched)
    }

    private fun moodle(token: String, function: String, extra: Map<String, String>, pages: PageFetch): String {
        val body = pages.load(
            REST,
            mapOf("wstoken" to token, "wsfunction" to function, "moodlewsrestformat" to "json") + extra
        )
        moodleFailure(body)?.let { throw ImportFailed(it, unauthorized = true) }
        return body
    }

    private fun courseFrom(row: List<Pair<String, String?>>): SyllabusCourse? {
        val href = row.getOrNull(2)?.second ?: return null
        val code = Regex("(?:^|[?&])lct_cd=([0-9]{10})(?:&|$)").find(href)?.groupValues?.get(1) ?: return null
        val term = row.getOrNull(6)?.first?.let { plain(it) }?.ifBlank { null }
        return SyllabusCourse(code = code, term = term, slotText = plain(row.getOrNull(7)?.first ?: ""))
    }

    private fun plain(value: String): String =
        Normalizer.normalize(value.replace(Regex("<[^>]*>"), ""), Normalizer.Form.NFKC).trim()

    private fun isoDate(year: Int, month: Int, day: Int): String =
        "%04d-%02d-%02d".format(year + if (month < 4) 1 else 0, month, day)
}

data class ReturnedToken(val siteId: String, val token: String)
data class DateSpan(val start: String, val end: String)
data class SyllabusCourse(
    val code: String,
    val term: String?,
    val slotText: String
)
data class ImportResult(val slots: Set<String>, val unmatched: Int)
class ImportFailed(message: String, val unauthorized: Boolean = false) : Exception(message)

fun interface PageFetch {
    fun load(url: String, form: Map<String, String>?): String
}

/** 岡大の公開ページとMoodle RESTへ、ホストごとのCookieを保ってHTTPSでアクセスする。 */
class HttpPages : PageFetch {
    private val cookies = mutableMapOf<String, MutableMap<String, String>>()

    override fun load(url: String, form: Map<String, String>?): String = exchange(url, form, 0)

    private fun exchange(url: String, form: Map<String, String>?, redirects: Int): String {
        if (redirects > 5) throw ImportFailed("大学の公開情報を取得できません")
        val parsed = java.net.URL(url)
        if (parsed.protocol != "https" || parsed.host !in hosts || parsed.port !in listOf(-1, 443) || parsed.userInfo != null) {
            throw ImportFailed("大学の公開URLが不正です")
        }
        val connection = parsed.openConnection() as java.net.HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("User-Agent", "KemoTaro/1.0")
        val jar = cookies[parsed.host]
        if (!jar.isNullOrEmpty()) {
            connection.setRequestProperty("Cookie", jar.entries.joinToString("; ") { "${it.key}=${it.value}" })
        }
        if (form != null) {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            val body = form.entries.joinToString("&") { entry ->
                java.net.URLEncoder.encode(entry.key, "UTF-8") + "=" + java.net.URLEncoder.encode(entry.value, "UTF-8")
            }
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }
        val code = connection.responseCode
        remember(parsed.host, connection.headerFields["Set-Cookie"])
        if (code in 300..399) {
            val location = connection.getHeaderField("Location") ?: throw ImportFailed("大学の公開情報を取得できません")
            connection.disconnect()
            return exchange(java.net.URL(parsed, location).toString(), null, redirects + 1)
        }
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            ?: throw ImportFailed("大学の公開情報を取得できません")
        val text = String(readLimited(stream), Charsets.UTF_8)
        if (code !in 200..299) throw ImportFailed("大学の公開情報を取得できません")
        return text
    }

    private fun remember(host: String, headers: List<String>?) {
        val jar = cookies.getOrPut(host) { mutableMapOf() }
        headers?.forEach { header ->
            val pair = header.substringBefore(';')
            val name = pair.substringBefore('=').trim()
            if (name.isNotEmpty()) jar[name] = pair.substringAfter('=', "").trim()
        }
    }

    private fun readLimited(stream: java.io.InputStream): ByteArray {
        val buffer = java.io.ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        stream.use { input ->
            while (true) {
                val count = input.read(chunk)
                if (count < 0) break
                if (buffer.size() + count > 4 * 1024 * 1024) throw ImportFailed("大学の公開ページが大きすぎます")
                buffer.write(chunk, 0, count)
            }
        }
        return buffer.toByteArray()
    }

    private companion object {
        val hosts = setOf(
            "moodle.el.okayama-u.ac.jp",
            "kyomu.adm.okayama-u.ac.jp",
            "www.okayama-u.ac.jp"
        )
    }
}

private sealed class Markup {
    data class Tag(val close: Boolean, val name: String, val attrs: Map<String, String>) : Markup()
    data class Text(val value: String) : Markup()
}

private fun tokens(html: String): List<Markup> {
    val result = mutableListOf<Markup>()
    for (match in Regex("<!--.*?-->|<[^>]+>|[^<]+", RegexOption.DOT_MATCHES_ALL).findAll(html)) {
        val token = match.value
        if (token.startsWith("<!--")) continue
        if (!token.startsWith("<")) {
            result += Markup.Text(unescapeHtml(token))
            continue
        }
        val parsed = Regex("^<\\s*(/?)([A-Za-z0-9]+)\\b([^>]*)>?$", RegexOption.DOT_MATCHES_ALL).matchEntire(token)
            ?: continue
        val attrs = linkedMapOf<String, String>()
        // 公開シラバスのリンクは href=DetailMain.aspx?lct_cd=... のように引用されない。
        Regex("([^\\s=]+)\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s\"'<>`]+))").findAll(parsed.groupValues[3])
            .forEach { attr ->
                val value = attr.groups[3]?.value ?: attr.groups[4]?.value ?: attr.groups[5]?.value ?: ""
                attrs[attr.groupValues[1].lowercase()] = unescapeHtml(value)
            }
        val raw = parsed.groupValues[3]
        if (Regex("\\bchecked\\b").containsMatchIn(raw) && "checked" !in attrs) attrs["checked"] = ""
        if (Regex("\\bselected\\b").containsMatchIn(raw) && "selected" !in attrs) attrs["selected"] = ""
        result += Markup.Tag(parsed.groupValues[1] == "/", parsed.groupValues[2].lowercase(), attrs)
    }
    return result
}

private fun unescapeHtml(text: String): String =
    Regex("&(#x[0-9A-Fa-f]+|#\\d+|[A-Za-z]+);").replace(text) { match ->
        val body = match.groupValues[1]
        val code = when {
            body == "amp" -> '&'.code
            body == "lt" -> '<'.code
            body == "gt" -> '>'.code
            body == "quot" -> '"'.code
            body == "apos" -> '\''.code
            body == "nbsp" -> ' '.code
            body.startsWith("#x") -> body.drop(2).toIntOrNull(16)
            body.startsWith("#") -> body.drop(1).toIntOrNull()
            else -> null
        }
        if (code != null && code in 0..0xFFFF) code.toChar().toString() else match.value
    }

private fun jsonObjects(json: String): List<String> {
    val text = json.trim().removePrefix("\uFEFF")
    if (!text.startsWith("[")) throw ImportFailed("Moodleを取得できません")
    val result = mutableListOf<String>()
    var index = 1
    while (index < text.length) {
        while (index < text.length && (text[index].isWhitespace() || text[index] == ',')) index++
        if (index >= text.length || text[index] == ']') break
        if (text[index] != '{') throw ImportFailed("Moodleを取得できません")
        val end = skipJson(text, index)
        result += text.substring(index, end)
        index = end
    }
    return result
}

private fun topFields(obj: String): Map<String, String> {
    val text = obj.trim()
    if (!text.startsWith("{")) throw ImportFailed("Moodleを取得できません")
    val fields = mutableMapOf<String, String>()
    var index = 1
    while (index < text.length) {
        while (index < text.length && (text[index].isWhitespace() || text[index] == ',')) index++
        if (index >= text.length || text[index] == '}') break
        if (text[index] != '"') throw ImportFailed("Moodleを取得できません")
        val keyEnd = skipJson(text, index)
        val key = unescapeJson(text.substring(index + 1, keyEnd - 1))
        index = keyEnd
        while (index < text.length && text[index].isWhitespace()) index++
        if (index >= text.length || text[index] != ':') throw ImportFailed("Moodleを取得できません")
        index++
        while (index < text.length && text[index].isWhitespace()) index++
        if (index >= text.length) throw ImportFailed("Moodleを取得できません")
        val valueEnd = skipJson(text, index)
        val raw = text.substring(index, valueEnd).trim()
        if (raw.startsWith("\"")) fields[key] = unescapeJson(raw.substring(1, raw.length - 1))
        else if (!raw.startsWith("{") && !raw.startsWith("[")) fields[key] = raw
        index = valueEnd
    }
    return fields
}

private fun skipJson(text: String, start: Int): Int {
    when (text[start]) {
        '"' -> {
            var index = start + 1
            while (index < text.length) {
                when (text[index]) {
                    '\\' -> index += 2
                    '"' -> return index + 1
                    else -> index++
                }
            }
            throw ImportFailed("Moodleを取得できません")
        }
        '{', '[' -> {
            var index = start
            var depth = 0
            while (index < text.length) {
                when (text[index]) {
                    '"' -> index = skipJson(text, index)
                    '{', '[' -> {
                        depth++
                        index++
                    }
                    '}', ']' -> {
                        depth--
                        index++
                        if (depth == 0) return index
                    }
                    else -> index++
                }
            }
            throw ImportFailed("Moodleを取得できません")
        }
        else -> {
            var index = start
            while (index < text.length && text[index] !in ",}]") index++
            return index
        }
    }
}

private fun unescapeJson(text: String): String = buildString {
    var index = 0
    while (index < text.length) {
        if (text[index] == '\\' && index + 1 < text.length) {
            when (text[index + 1]) {
                'u' -> {
                    if (index + 6 > text.length) throw ImportFailed("Moodleを取得できません")
                    val code = text.substring(index + 2, index + 6).toIntOrNull(16)
                    if (code == null || code !in 0..0xFFFF) throw ImportFailed("Moodleを取得できません")
                    append(code.toChar())
                    index += 6
                }
                'n' -> { append('\n'); index += 2 }
                '"' -> { append('"'); index += 2 }
                '\\' -> { append('\\'); index += 2 }
                '/' -> { append('/'); index += 2 }
                else -> { append(text[index + 1]); index += 2 }
            }
        } else {
            append(text[index])
            index++
        }
    }
}

private fun decodeBase64(text: String): ByteArray {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val clean = buildString { text.forEach { if (!it.isWhitespace()) append(it) } }
    if (clean.isEmpty() || clean.length % 4 == 1) throw IllegalArgumentException()
    val out = ArrayList<Byte>(clean.length)
    var buffer = 0
    var bits = 0
    for (char in clean) {
        if (char == '=') break
        val value = alphabet.indexOf(char)
        if (value < 0) throw IllegalArgumentException()
        buffer = (buffer shl 6) or value
        bits += 6
        if (bits >= 8) {
            bits -= 8
            out += ((buffer shr bits) and 0xff).toByte()
        }
    }
    return out.toByteArray()
}
