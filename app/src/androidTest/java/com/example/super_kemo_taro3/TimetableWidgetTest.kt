package com.example.super_kemo_taro3

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.backend.repository.CourseLinks
import com.example.super_kemo_taro3.backend.service.LessonTime
import com.example.super_kemo_taro3.backend.service.NextLesson
import com.example.super_kemo_taro3.backend.service.TimetableLesson
import com.example.super_kemo_taro3.backend.service.TimetableSlot
import com.example.super_kemo_taro3.frontend.screens.HomeActivity
import com.example.super_kemo_taro3.frontend.screens.MainDestination
import com.example.super_kemo_taro3.frontend.widget.TimetableWidgetProvider
import com.example.super_kemo_taro3.frontend.widget.TimetableWidgetState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.io.File
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class TimetableWidgetTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val monday = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, 26, 8, 0)
    }.timeInMillis
    private val slot = TimetableSlot("MON_1", Calendar.MONDAY, 1)
    private val course = CourseLinks("情報処理", classroom = "一般教育棟 A21教室")

    private fun state() = TimetableWidgetState(
        now = monday,
        nextLesson = NextLesson(slot, monday + 40 * 60_000, LessonTime(9, 30), course.subject, listOf(course)),
        periods = listOf(TimetableLesson(slot, LessonTime(8, 40), LessonTime(9, 30), course.subject, listOf(course))) +
            List(7) { null },
        refreshAt = monday + 40 * 60_000 + 1
    )

    @Test fun remoteViewsKeepContentTogetherAtCompactAndLargeSizes() {
        instrumentation.runOnMainSync {
            var compactTextSize = 0f
            var compactCellSize = 0
            for ((width, height) in listOf(250 to 110, 360 to 110, 360 to 180, 400 to 224)) {
                val root = TimetableWidgetProvider.createViews(context, state(), width.toFloat(), height.toFloat())
                    .apply(context, FrameLayout(context))
                val density = context.resources.displayMetrics.density
                root.measure(
                    View.MeasureSpec.makeMeasureSpec((width * density).roundToInt(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec((height * density).roundToInt(), View.MeasureSpec.EXACTLY)
                )
                root.layout(0, 0, root.measuredWidth, root.measuredHeight)
                assertEquals(course.subject, root.findViewById<TextView>(R.id.widget_subject).text.toString())
                assertEquals(course.classroom, root.findViewById<TextView>(R.id.widget_classroom).text.toString())
                val notes = root.findViewById<TextView>(R.id.widget_notes)
                assertEquals(context.getString(R.string.widget_notes_none), notes.text.toString())
                assertTrue(notes.top >= root.findViewById<View>(R.id.widget_classroom).bottom)
                assertTrue(root.findViewById<View>(R.id.widget_next_time).top >= notes.bottom)
                val periods = root.findViewById<LinearLayout>(R.id.widget_periods)
                val header = root.findViewById<View>(R.id.widget_header)
                // 大きい配置でも、授業情報と8コマの間に余分な高さを挟まない。
                assertTrue(periods.top - header.bottom <= (9 * density).roundToInt())
                assertTrue(periods.bottom <= root.height - root.paddingBottom)
                assertTrue(header.top >= root.paddingTop)
                assertEquals(8, periods.childCount)
                assertEquals("1限", (periods.getChildAt(0) as TextView).text.toString())
                assertEquals("2限", (periods.getChildAt(1) as TextView).text.toString())
                for (index in 0..7) {
                    val cell = periods.getChildAt(index)
                    assertEquals("${width}x${height}・${index + 1}限", cell.width, cell.height)
                    assertTrue(cell.right <= periods.width)
                    assertTrue(cell.hasOnClickListeners())
                }
                assertTrue(root.hasOnClickListeners())
                val textSize = root.findViewById<TextView>(R.id.widget_subject).textSize
                if (width == 250) {
                    compactTextSize = textSize
                    compactCellSize = periods.getChildAt(0).width
                } else if (height > 110) {
                    assertTrue(textSize > compactTextSize)
                    assertTrue(periods.getChildAt(0).width > compactCellSize)
                }
                // 実際にRemoteViewsを描画した画像を、レイアウトの目視確認用に保存する。
                val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
                root.draw(Canvas(bitmap))
                val output = File(context.getExternalFilesDir("widget-previews"), "widget-${width}x${height}.png")
                output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
    }

    @Test fun emptyTimetableHasHelpfulTextAndEightEmptyCells() {
        instrumentation.runOnMainSync {
            val empty = TimetableWidgetState(monday, null, List(8) { null }, monday + 86_400_000)
            val root = TimetableWidgetProvider.createViews(context, empty, 300f).apply(context, FrameLayout(context))
            assertEquals(context.getString(R.string.widget_no_lessons),
                root.findViewById<TextView>(R.id.widget_subject).text.toString())
            assertEquals(context.getString(R.string.widget_notes_none),
                root.findViewById<TextView>(R.id.widget_notes).text.toString())
            assertEquals(context.getString(R.string.widget_today_empty),
                root.findViewById<LinearLayout>(R.id.widget_periods).contentDescription.toString())
            assertEquals(8, root.findViewById<LinearLayout>(R.id.widget_periods).childCount)
        }
    }

    @Test fun futureSquaresAnnounceTheDisplayedDateIncludingMonday() {
        instrumentation.runOnMainSync {
            for ((day, label) in listOf(27 to "10/27（火）", 33 to "11/2（月）")) {
                val date = Calendar.getInstance().apply {
                    clear()
                    set(2026, Calendar.OCTOBER, day)
                }.timeInMillis
                for (hasLessons in listOf(true, false)) {
                    val future = state().copy(
                        periodsDate = date,
                        periods = if (hasLessons) state().periods else List(8) { null }
                    )
                    val root = TimetableWidgetProvider.createViews(context, future, 300f)
                        .apply(context, FrameLayout(context))
                    val description = if (hasLessons) "${label}の1〜8限" else "${label}は授業がありません"
                    assertEquals(description,
                        root.findViewById<LinearLayout>(R.id.widget_periods).contentDescription.toString())
                }
            }
        }
    }

    @Test fun noteAvailabilityIsDisplayedAndAnnouncedForBothStates() {
        instrumentation.runOnMainSync {
            for (hasNote in listOf(true, false)) {
                val root = TimetableWidgetProvider.createViews(context, state().copy(hasNextLessonNote = hasNote), 300f)
                    .apply(context, FrameLayout(context))
                val expected = context.getString(if (hasNote) R.string.widget_notes_available else R.string.widget_notes_none)
                val notes = root.findViewById<TextView>(R.id.widget_notes)
                assertEquals(expected, notes.text.toString())
                assertEquals(expected, notes.contentDescription.toString())
            }
        }
    }

    @Test fun widgetIsDiscoverableAndTapExplicitlyOpensTheHomeDestination() {
        val component = ComponentName(context, TimetableWidgetProvider::class.java)
        assertTrue(AppWidgetManager.getInstance(context).installedProviders.any { it.provider == component })
        val intent = TimetableWidgetProvider.homeIntent(context)
        assertEquals(ComponentName(context, HomeActivity::class.java), intent.component)
        assertEquals(MainDestination.Home.name, intent.getStringExtra(HomeActivity.EXTRA_DESTINATION))
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
    }
}
