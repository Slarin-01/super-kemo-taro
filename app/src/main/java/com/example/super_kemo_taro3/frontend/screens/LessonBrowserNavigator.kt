package com.example.super_kemo_taro3.frontend.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Browser
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.super_kemo_taro3.backend.repository.CourseLinks
import com.example.super_kemo_taro3.backend.service.CourseLinkRules
import com.example.super_kemo_taro3.backend.service.TimetableLesson

/** 授業の2つのページを、外部ブラウザの新しいタブで開く。 */
object LessonBrowserNavigator {
    fun open(activity: AppCompatActivity, lesson: TimetableLesson) {
        val courses = lesson.courseLinks.distinct().filter { CourseLinkRules.browserUrls(it).isNotEmpty() }
        when (courses.size) {
            0 -> showMessage(activity, "リンクが未登録です。設定の「履修を取り込む」から再取り込みしてください。")
            1 -> openCourse(activity, courses.single())
            else -> AlertDialog.Builder(activity)
                .setTitle("開く授業を選択")
                .setItems(courses.map { it.subject.ifBlank { "教科未設定" } }.toTypedArray()) { _, index ->
                    openCourse(activity, courses[index])
                }
                .setNegativeButton("キャンセル", null)
                .show()
        }
    }

    @Suppress("DEPRECATION")
    private fun openCourse(activity: AppCompatActivity, course: CourseLinks) {
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
            preferred != null -> openPages(activity, course, preferred.activityInfo.packageName)
            browsers.isEmpty() -> showMessage(activity, "利用できる外部ブラウザが見つかりません。")
            else -> AlertDialog.Builder(activity)
                .setTitle("ブラウザを選択")
                .setItems(browsers.map { it.loadLabel(manager).toString() }.toTypedArray()) { _, index ->
                    openPages(activity, course, browsers[index].activityInfo.packageName)
                }
                .setNegativeButton("キャンセル", null)
                .show()
        }
    }

    private fun openPages(activity: AppCompatActivity, course: CourseLinks, browserPackage: String) {
        val urls = CourseLinkRules.browserUrls(course)
        val intents = urls.map { url ->
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setPackage(browserPackage)
                .putExtra(Browser.EXTRA_CREATE_NEW_TAB, true)
        }.toTypedArray()
        try {
            activity.startActivities(intents)
            if (urls.size < 2) {
                showMessage(activity, "取得済みのページを開きました。両方のリンクを取得するには履修を再取り込みしてください。")
            }
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
