package com.example.super_kemo_taro3.frontend.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Browser
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.super_kemo_taro3.backend.service.CourseLinkRules
import com.example.super_kemo_taro3.backend.service.CoursePage
import com.example.super_kemo_taro3.backend.service.TimetableLesson

/** 授業のMoodleまたはシラバスを、外部ブラウザで開く。 */
object LessonBrowserNavigator {
    fun open(activity: AppCompatActivity, lesson: TimetableLesson, page: CoursePage) {
        val courses = lesson.courseLinks.filter { CourseLinkRules.browserUrl(it, page) != null }
            .distinctBy { CourseLinkRules.browserUrl(it, page) }
        val pageLabel = if (page == CoursePage.MOODLE) "Moodle" else "シラバス"
        when (courses.size) {
            0 -> showMessage(activity, "${pageLabel}のリンクが未登録です。設定の「履修を取り込む」から再取り込みしてください。")
            1 -> openCourse(activity, requireNotNull(CourseLinkRules.browserUrl(courses.single(), page)))
            else -> AlertDialog.Builder(activity)
                .setTitle("${pageLabel}を開く授業を選択")
                .setItems(courses.map { it.subject.ifBlank { "教科未設定" } }.toTypedArray()) { _, index ->
                    openCourse(activity, requireNotNull(CourseLinkRules.browserUrl(courses[index], page)))
                }
                .setNegativeButton("キャンセル", null)
                .show()
        }
    }

    @Suppress("DEPRECATION")
    private fun openCourse(activity: AppCompatActivity, url: String) {
        val manager = activity.packageManager
        // 特定の大学URLを受け取るアプリではなく、HTTPS全般を開けるブラウザを調べる。
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val browsers = manager.queryIntentActivities(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .filter { it.activityInfo.exported }
            .distinctBy { it.activityInfo.packageName }
        val defaultPackage = manager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
        val preferred = browsers.firstOrNull { it.activityInfo.packageName == defaultPackage }
            ?: browsers.singleOrNull()
        when {
            preferred != null -> openPage(activity, url, preferred.activityInfo.packageName)
            browsers.isEmpty() -> showMessage(activity, "利用できる外部ブラウザが見つかりません。")
            else -> AlertDialog.Builder(activity)
                .setTitle("ブラウザを選択")
                .setItems(browsers.map { it.loadLabel(manager).toString() }.toTypedArray()) { _, index ->
                    openPage(activity, url, browsers[index].activityInfo.packageName)
                }
                .setNegativeButton("キャンセル", null)
                .show()
        }
    }

    private fun openPage(activity: AppCompatActivity, url: String, browserPackage: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setPackage(browserPackage)
            .putExtra(Browser.EXTRA_CREATE_NEW_TAB, true)
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            showMessage(activity, "ブラウザを起動できませんでした。")
        } catch (_: SecurityException) {
            showMessage(activity, "ブラウザを起動できませんでした。")
        }
    }

    private fun showMessage(activity: AppCompatActivity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}
