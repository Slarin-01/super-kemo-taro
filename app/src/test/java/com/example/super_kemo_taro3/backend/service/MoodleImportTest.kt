package com.example.super_kemo_taro3.backend.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Moodleの科目一覧、公開シラバス、学年暦から、今日ミュートする枠を決める。 */
class MoodleImportTest {
    private val today = "2026-10-18"
    private val nowEpoch = 1_700_000_000L

    @Test fun returnedTokenKeepsSiteIdAndToken() {
        val site = "a".repeat(32)
        val token = "b".repeat(32)
        val encoded = java.util.Base64.getEncoder().encodeToString("$site:::$token".toByteArray())
        val returned = MoodleImport.returnedToken("kemotaro://token=$encoded")
        assertEquals(site, returned?.siteId)
        assertEquals(token, returned?.token)
        assertEquals(MoodleImport.passportSiteId("abc").length, 32)
        assertNull(MoodleImport.returnedToken("https://example.test/token=$encoded"))
    }

    @Test fun tokyoTodayUsesJapanDate() {
        assertEquals("1970-01-01", MoodleImport.tokyoToday(-9L * 60 * 60 * 1000))
    }

    @Test fun lectureCodesSkipEndedCoursesAndReadBracketedIds() {
        val json = """
            [
              {"idnumber":"","fullname":"[2026010004] 特別研究","enddate":0},
              {"idnumber":"2026010001","fullname":"情報処理","enddate":0,"overviewfiles":[]},
              {"idnumber":"2025092030","fullname":"終了した科目","enddate":1},
              {"idnumber":"","fullname":"案内","enddate":0}
            ]
        """.trimIndent()
        assertEquals(listOf("2026010004", "2026010001"), MoodleImport.lectureCodes(json, nowEpoch))
    }

    @Test fun moodleErrorUsesServerMessage() {
        assertEquals("無効なトークン", MoodleImport.moodleFailure("""{"exception":"x","message":"無効なトークン"}"""))
    }

    @Test fun currentTermSlotsIgnoreOtherTermsWeekendsAndLaterPeriods() {
        val pages = FixturePages()
        val result = MoodleImport.collect("token", today, nowEpoch, pages)
        assertEquals(setOf("THU_5", "THU_6", "TUE_1", "FRI_8"), result.slots)
        assertEquals(2, result.unmatched)
        assertTrue(pages.posts.all { it["__VIEWSTATE"] == "view" && it[searchButton] == "検 索" })
        assertEquals(listOf("2026010001", "2026010002", "2026010003", "2026010004", "2026010005"), pages.codes)
    }

    @Test fun rejectsUnlistedHostsBeforeConnecting() {
        val error = runCatching { HttpPages().load("https://example.test/", null) }.exceptionOrNull()
        assertEquals("大学の公開URLが不正です", error?.message)
    }

    @Test fun emptyEnrolmentDoesNotReadTheCalendar() {
        val pages = FixturePages(courses = """[{"idnumber":"","fullname":"案内","enddate":0}]""")
        val error = runCatching { MoodleImport.collect("token", today, nowEpoch, pages) }.exceptionOrNull()
        assertEquals("履修中の科目を読み取れません", error?.message)
        assertTrue(pages.loads.none { it == MoodleImport.CALENDAR_URL })
    }
}

private class FixturePages(
    private val courses: String = """
        [
          {"idnumber":"2026010001","fullname":"情報処理","enddate":0},
          {"idnumber":"2026010002","fullname":"前期の授業","enddate":0},
          {"idnumber":"2026010003","fullname":"土曜","enddate":0},
          {"idnumber":"","fullname":"[2026010004] 通年","enddate":0},
          {"idnumber":"2026010005","fullname":"夜間","enddate":0},
          {"idnumber":"2025092030","fullname":"終了","enddate":1}
        ]
    """.trimIndent()
) : PageFetch {
    val loads = mutableListOf<String>()
    val posts = mutableListOf<Map<String, String>>()
    val codes = mutableListOf<String>()

    override fun load(url: String, form: Map<String, String>?): String {
        loads += url
        if (form != null && url == MoodleImport.SEARCH_URL) {
            posts += form
            codes += form.getValue(codeField)
        }
        return when {
            form?.get("wsfunction") == "core_webservice_get_site_info" -> """{"userid":7}"""
            form?.get("wsfunction") == "core_enrol_get_users_courses" -> courses
            url == MoodleImport.CALENDAR_URL -> calendar
            form == null -> formPage
            else -> syllabus(form.getValue(codeField))
        }
    }
}

private val codeField = "ctl00${"$"}phContents${"$"}txt_lctCd_Search"
private val searchButton = "ctl00${"$"}phContents${"$"}ctl18${"$"}btnSearch"
private val formPage = """
    <input type="hidden" name="__VIEWSTATE" value="view" />
    <select name="ctl00${"$"}phContents${"$"}ddl_year"><option selected value="2026">2026</option></select>
    <input name="$codeField" value="" />
"""

private const val calendar = """
    <h2>学期</h2>
    <h3>2026年度</h3>
    <table>
      <tr><td>第1学期</td><td>4月1日〜6月9日</td><td>第2学期</td><td>6月10日〜8月10日</td></tr>
      <tr><td>第3学期</td><td>10月1日〜12月3日</td><td>第4学期</td><td>12月4日〜2月14日</td></tr>
      <tr><td>前期</td><td>4月1日〜9月30日</td><td>後期</td><td>10月1日〜3月31日</td></tr>
    </table>
"""

private fun syllabus(code: String): String {
    val row = when (code) {
        "2026010001" -> cells(code, "第３学期", "木5〜6 月1")
        "2026010002" -> cells(code, "前期", "月1")
        "2026010003" -> cells(code, "第3学期", "土1")
        "2026010004" -> cells(code, "通年", "火1")
        else -> cells(code, "第3学期", "金8〜10")
    }
    return """<table id="ctl00_phContents_ucGrid_grv"><tr>$row</tr></table>"""
}

private fun cells(code: String, term: String, slot: String) =
    "<td>1</td><td>2026</td><td><a href=DetailMain.aspx?lct_year=2026&lct_cd=$code&je_cd=1 target=_blank>講義</a></td><td></td><td>科目</td><td>教員</td><td>$term</td><td>$slot</td>"
