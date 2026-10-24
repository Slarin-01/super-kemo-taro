package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.CourseLinks
import java.net.URI

/** 履修先のコースIDと、シラバスが返した詳細リンクからブラウザ用URLを作る。 */
object CourseLinkRules {
    private val moodle = URI(MoodleImport.ORIGIN)
    private val syllabus = URI(MoodleImport.SEARCH_URL)
    private const val SYLLABUS_PATH = "/Portal/Public/Syllabus/DetailMain.aspx"

    fun moodleUrl(id: Long?): String? =
        id?.takeIf { it > 0 }?.let { "${MoodleImport.ORIGIN}/course/view.php?id=$it" }

    fun syllabusUrl(href: String): String? = runCatching {
        syllabus.resolve(href).normalize().takeIf {
            allowed(it, syllabus.host, SYLLABUS_PATH) &&
                it.rawQuery.orEmpty().split('&').any { field ->
                    Regex("lct_cd=[0-9A-Za-z]{10}").matches(field)
                }
        }?.toASCIIString()
    }.getOrNull()

    /** 保存済みのリンクも大学のHTTPSページだけを起動対象にする。 */
    fun browserUrls(links: CourseLinks): List<String> = listOfNotNull(
        links.moodleUrl?.takeIf { value ->
            runCatching {
                val uri = URI(value)
                allowed(uri, moodle.host, "/course/view.php") &&
                    Regex("id=[1-9][0-9]*").matches(uri.rawQuery.orEmpty())
            }.getOrDefault(false)
        },
        links.syllabusUrl?.takeIf { syllabusUrl(it) == it }
    ).distinct()

    private fun allowed(uri: URI, host: String, path: String): Boolean =
        uri.scheme == "https" && uri.host.equals(host, ignoreCase = true) &&
            uri.port in listOf(-1, 443) && uri.userInfo == null && uri.path == path
}
