package com.example.super_kemo_taro3.frontend.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.super_kemo_taro3.R
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.backend.repository.CourseLinks
import com.example.super_kemo_taro3.backend.service.LessonTimeRules
import com.example.super_kemo_taro3.frontend.screens.HomeActivity
import com.example.super_kemo_taro3.frontend.screens.MainDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** ランチャーに表示する時間割。通知の自動ミュートとは独立して更新する。 */
class TimetableWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = updateAll(context)

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) =
        updateAll(context)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH, Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> updateAll(context)
        }
    }

    override fun onDisabled(context: Context) = cancelRefresh(context)

    companion object {
        private const val ACTION_REFRESH = "com.example.super_kemo_taro3.REFRESH_TIMETABLE_WIDGET"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TimetableWidgetProvider::class.java))
            if (ids.isEmpty()) {
                cancelRefresh(context)
                return
            }
            val services = context.appServices
            val state = TimetableWidgetState.read(services.timetable, notes = services.notes.readNotes())
            ids.forEach { id ->
                manager.updateAppWidget(id, sizedViews(context, manager.getAppWidgetOptions(id), state))
            }
            scheduleRefresh(context, state.refreshAt)
        }

        private fun sizedViews(context: Context, options: Bundle, state: TimetableWidgetState): RemoteViews {
            // Android 12以降はランチャーが提供する各サイズ、旧端末は縦・横の2サイズに対応する。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                @Suppress("DEPRECATION")
                val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                if (!sizes.isNullOrEmpty()) {
                    return RemoteViews(sizes.distinct().take(16).associateWith {
                        createViews(context, state, it.width, it.height)
                    })
                }
            }
            val narrow = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 300).coerceAtLeast(1)
            val wide = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, narrow).coerceAtLeast(narrow)
            val short = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110).coerceAtLeast(1)
            val tall = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, short).coerceAtLeast(short)
            return RemoteViews(
                createViews(context, state, wide.toFloat(), short.toFloat()),
                createViews(context, state, narrow.toFloat(), tall.toFloat())
            )
        }

        /** 明示的なホーム遷移。設定タブや編集画面が開いていてもHomeActivityのホームに戻す。 */
        internal fun homeIntent(context: Context): Intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(HomeActivity.EXTRA_DESTINATION, MainDestination.Home.name)
        }

        internal fun createViews(
            context: Context,
            state: TimetableWidgetState,
            widthDp: Float,
            heightDp: Float = 110f
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.timetable_widget)
            val resources = context.resources
            val metrics = resources.displayMetrics
            // 最小サイズの見やすさを保ちつつ、配置枠の縦横両方に余裕があれば全体を拡大する。
            val scale = minOf(widthDp / 250f, heightDp / 110f).coerceIn(1f, 2f)
            val padding = (resources.getDimensionPixelSize(R.dimen.widget_padding) * scale).roundToInt()
            views.setViewPadding(R.id.widget_root, padding, padding, padding, padding)
            mapOf(
                R.id.widget_next_label to 10f,
                R.id.widget_subject to 15f,
                R.id.widget_classroom to 11f,
                R.id.widget_notes to 11f,
                R.id.widget_next_time to 10f
            ).forEach { (id, size) -> views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, size * scale) }
            val openHome = PendingIntent.getActivity(
                context, 0, homeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openHome)
            val next = state.nextLesson
            val courses = next?.courseLinks.orEmpty().distinctBy { it.subject to it.classroom }
                .ifEmpty { listOf(CourseLinks(next?.subject.orEmpty())) }
            val subject = if (next == null) context.getString(R.string.widget_no_lessons) else
                courses.joinToString(" / ") { it.subject.ifBlank { context.getString(R.string.widget_no_subject) } }
            val classroom = if (next == null) context.getString(R.string.widget_setup_hint) else
                courses.joinToString(" / ") { it.classroom?.takeIf(String::isNotBlank)
                    ?: context.getString(R.string.widget_no_classroom) }
            views.setTextViewText(R.id.widget_subject, subject)
            views.setContentDescription(R.id.widget_subject, subject)
            views.setTextViewText(R.id.widget_classroom, classroom)
            views.setContentDescription(R.id.widget_classroom, classroom)
            val notesText = context.getString(if (state.hasNextLessonNote)
                R.string.widget_notes_available else R.string.widget_notes_none)
            views.setTextViewText(R.id.widget_notes, notesText)
            views.setContentDescription(R.id.widget_notes, notesText)
            val whenText = if (next == null) "" else {
                val day = if (format("yyyyMMdd", next.startsAt) == format("yyyyMMdd", state.now)) {
                    context.getString(R.string.widget_today)
                } else format("M/d（E）", next.startsAt)
                context.getString(R.string.widget_next_time, day, next.slot.lesson,
                    format("HH:mm", next.startsAt), LessonTimeRules.formatTime(next.endTime))
            }
            views.setTextViewText(R.id.widget_next_time, whenText)
            val hasLessons = state.periods.any { it != null }
            val periodsDescription = if (state.showingTomorrow) {
                if (hasLessons) R.string.widget_tomorrow_periods else R.string.widget_tomorrow_empty
            } else {
                if (hasLessons) R.string.widget_today_periods else R.string.widget_today_empty
            }
            views.setContentDescription(R.id.widget_periods, context.getString(periodsDescription))
            // 文字と同じ倍率で8個の正方形も拡大する。左右の余白を引いた幅を超えないようにする。
            val available = (widthDp * metrics.density).toInt() - 2 * padding -
                16 * resources.getDimensionPixelSize(R.dimen.widget_period_margin)
            val pixels = (available / 8).coerceIn(1,
                (resources.getDimensionPixelSize(R.dimen.widget_period_max_size) * scale).toInt())
            views.removeAllViews(R.id.widget_periods)
            state.periods.forEachIndexed { index, lesson ->
                val period = index + 1
                val cell = RemoteViews(context.packageName, R.layout.timetable_widget_period)
                cell.setTextViewText(R.id.widget_period, context.getString(R.string.widget_period, period))
                cell.setInt(R.id.widget_period, "setWidth", pixels)
                cell.setInt(R.id.widget_period, "setHeight", pixels)
                // 時限番号だけを正方形の中央に1行で表示する。
                val textPixels = minOf(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f * scale, metrics), pixels * .48f
                )
                cell.setTextViewTextSize(R.id.widget_period, TypedValue.COMPLEX_UNIT_PX, textPixels)
                cell.setInt(R.id.widget_period, "setBackgroundResource", if (lesson == null)
                    R.drawable.widget_period_empty else R.drawable.widget_period_filled)
                cell.setTextColor(R.id.widget_period, context.getColor(if (lesson == null)
                    R.color.widget_muted else R.color.widget_on_accent))
                cell.setContentDescription(R.id.widget_period, if (lesson == null)
                    context.getString(R.string.widget_period_empty_description, period) else
                    context.getString(R.string.widget_period_description, period,
                        LessonTimeRules.formatTime(lesson.startTime), LessonTimeRules.formatTime(lesson.endTime),
                        lesson.subject ?: context.getString(R.string.widget_no_subject)))
                cell.setOnClickPendingIntent(R.id.widget_period, openHome)
                views.addView(R.id.widget_periods, cell)
            }
            return views
        }

        private fun format(pattern: String, time: Long): String =
            SimpleDateFormat(pattern, Locale.JAPAN).format(Date(time))

        private fun refreshIntent(context: Context, flags: Int): PendingIntent? = PendingIntent.getBroadcast(
            context, 0, Intent(context, TimetableWidgetProvider::class.java).setAction(ACTION_REFRESH),
            flags or PendingIntent.FLAG_IMMUTABLE
        )

        private fun scheduleRefresh(context: Context, time: Long) {
            val alarms = context.getSystemService(AlarmManager::class.java)
            val pending = requireNotNull(refreshIntent(context, PendingIntent.FLAG_UPDATE_CURRENT))
            // 表示更新では端末を起こさない。正確なアラームが使えなくてもウィジェットは動作する。
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()) {
                try {
                    alarms.setExact(AlarmManager.RTC, time, pending)
                    return
                } catch (_: SecurityException) {
                    // 確認後に権限が取り消された場合も通常の予約へフォールバックする。
                }
            }
            alarms.set(AlarmManager.RTC, time, pending)
        }

        private fun cancelRefresh(context: Context) {
            val pending = refreshIntent(context, PendingIntent.FLAG_NO_CREATE) ?: return
            context.getSystemService(AlarmManager::class.java).cancel(pending)
            pending.cancel()
        }
    }
}
