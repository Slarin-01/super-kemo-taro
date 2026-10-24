package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.CourseLinks
import org.junit.Assert.*
import org.junit.Test

class CourseLinkRulesTest {
    private val detail = "DetailMain.aspx?lct_year=2026&lct_cd=202602B409&je_cd=1"
    private val syllabus = "https://kyomu.adm.okayama-u.ac.jp/Portal/Public/Syllabus/$detail"

    @Test fun moodleLinksUseTheActualCourseId() {
        assertEquals("${MoodleImport.ORIGIN}/course/view.php?id=409", CourseLinkRules.moodleUrl(409))
        assertNull(CourseLinkRules.moodleUrl(null))
        assertNull(CourseLinkRules.moodleUrl(0))
        assertNull(CourseLinkRules.moodleUrl(-1))
    }

    @Test fun preservesTheSyllabusDetailParametersWhenResolvingARelativeLink() {
        assertEquals(syllabus, CourseLinkRules.syllabusUrl(detail))
        assertEquals(syllabus, CourseLinkRules.syllabusUrl(syllabus))
        assertEquals(syllabus, CourseLinkRules.syllabusUrl("/Portal/Public/Syllabus/$detail"))
    }

    @Test fun invalidOrExternalSyllabusTargetsAreIgnored() {
        listOf(
            "javascript:alert(1)", "https://example.test/$detail", "//example.test/$detail",
            syllabus.replace("https:", "http:"),
            syllabus.replace("https://", "https://user:password@"),
            "DetailMain.aspx", "SearchMain.aspx?lct_cd=202602B409"
        ).forEach { assertNull(CourseLinkRules.syllabusUrl(it)) }
    }

    @Test fun returnsBothCoursePagesInOrder() {
        val moodle = requireNotNull(CourseLinkRules.moodleUrl(409))
        assertEquals(listOf(moodle, syllabus), CourseLinkRules.browserUrls(CourseLinks("実験", moodle, syllabus)))
    }

    @Test fun legacyAndMalformedStoredLinksDoNotBecomeBrowserTargets() {
        assertTrue(CourseLinkRules.browserUrls(CourseLinks("実験")).isEmpty())
        assertEquals(listOf(syllabus), CourseLinkRules.browserUrls(CourseLinks(
            "実験", "${MoodleImport.ORIGIN}/course/view.php?id=409&wstoken=secret", syllabus
        )))
        assertTrue(CourseLinkRules.browserUrls(CourseLinks(
            "実験", "intent://course/409", "https://example.test/$detail"
        )).isEmpty())
    }
}
