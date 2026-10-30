package com.example.super_kemo_taro3.frontend.widget

import com.example.super_kemo_taro3.backend.repository.Note
import com.example.super_kemo_taro3.backend.service.NextLesson
import com.example.super_kemo_taro3.backend.service.TimetableLesson
import com.example.super_kemo_taro3.backend.service.TimetableService
import java.util.Calendar

/** 次の授業はホームと共通。8個の枠は、当日の全授業が終了したら翌日を示す。 */
data class TimetableWidgetState(
    val now: Long,
    val nextLesson: NextLesson?,
    val periods: List<TimetableLesson?>,
    val refreshAt: Long,
    val showingTomorrow: Boolean = false,
    val hasNextLessonNote: Boolean = false
) {
    companion object {
        fun read(
            timetable: TimetableService,
            now: Long = System.currentTimeMillis(),
            notes: List<Note> = emptyList()
        ): TimetableWidgetState {
            val today = Calendar.getInstance().apply { timeInMillis = now }
            val todayLessons = timetable.readLessonsForDay(today[Calendar.DAY_OF_WEEK])
            val lastEnd = todayLessons.maxOfOrNull { lesson ->
                (today.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, lesson.endTime.hour)
                    set(Calendar.MINUTE, lesson.endTime.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            val next = timetable.readNextLesson(now)
            val subjects = next?.courseLinks.orEmpty().map { it.subject.trim() }
                .filter { it.isNotEmpty() }.ifEmpty { listOfNotNull(next?.subject?.trim()) }
                .filter { it.isNotEmpty() }.toSet()
            // 教科なしや別の教科、期限切れのメモは「あり」の対象にしない。
            val nextLessonNotes = notes.filter { it.subject.trim() in subjects && !it.isExpired(now) }
            val noteDeadline = nextLessonNotes.mapNotNull { it.dueAt }.minOrNull()
            val tomorrow = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val midnight = tomorrow.timeInMillis
            // 授業がない日は当日を表示し、終了後も授業のない翌日を飛ばさない。
            val showingTomorrow = lastEnd != null && now >= lastEnd
            val lessons = (if (showingTomorrow) {
                timetable.readLessonsForDay(tomorrow[Calendar.DAY_OF_WEEK])
            } else todayLessons).associateBy { it.slot.lesson }
            return TimetableWidgetState(
                now = now,
                nextLesson = next,
                periods = (1..8).map { lessons[it] },
                // ホームは開始時刻ちょうどまでその授業を表示するため、1ms後に進める。
                refreshAt = minOf(
                    midnight,
                    next?.startsAt?.plus(1L) ?: midnight,
                    lastEnd?.takeIf { it > now } ?: midnight,
                    noteDeadline ?: midnight
                ),
                showingTomorrow = showingTomorrow,
                hasNextLessonNote = nextLessonNotes.isNotEmpty()
            )
        }
    }
}
