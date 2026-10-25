/*
 * フロントエンド・画面: ホーム・設定の起動、状態の接続、下部ナビゲーションの表示。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.backend.service.LessonTime
import com.example.super_kemo_taro3.backend.service.CoursePage
import com.example.super_kemo_taro3.backend.service.LessonTimeRules
import com.example.super_kemo_taro3.backend.service.NextLesson
import com.example.super_kemo_taro3.backend.service.TimetableLesson
import com.example.super_kemo_taro3.backend.service.TimetableSlot
import com.example.super_kemo_taro3.frontend.viewmodel.HomeViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.MenuViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 1. ホーム画面の起動とViewModelへの操作の接続
/** ホームと設定を同じActivityで表示し、下部バーを保持したまま本文を切り替える。 */
class HomeActivity : AppCompatActivity() {
    private var destination by mutableStateOf(MainDestination.Home)
    private lateinit var menuModel: MenuViewModel

    private val model by lazy {
        val services = appServices
        ViewModelProvider(this, ScreenViewModelFactory {
            HomeViewModel(services.timetable, services.pause, services.notifications)
        })[HomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        destination = restoredDestination(
            savedInstanceState?.getString(EXTRA_DESTINATION) ?: intent.getStringExtra(EXTRA_DESTINATION)
        )
        menuModel = ViewModelProvider(this, ScreenViewModelFactory {
            MenuViewModel(savedInstanceState?.getBoolean("show_credits", false) ?: false)
        })[MenuViewModel::class.java]
        model.refresh()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                // 授業開始直後に次の予定へ進め、開いたままの画面にも時刻の経過を反映する。
                while (true) {
                    model.refresh()
                    val untilNextLesson = model.uiState.nextLesson?.startsAt?.let {
                        (it - System.currentTimeMillis() + 1L).coerceIn(1L, 60_000L)
                    } ?: 60_000L
                    delay(untilNextLesson)
                }
            }
        }
        setContent {
            AppScreen {
                BackHandler(enabled = destination == MainDestination.Settings) {
                    selectDestination(MainDestination.Home)
                }
                MainScreenScaffold(
                    destination = destination,
                    onHome = { selectDestination(MainDestination.Home) },
                    onSettings = { selectDestination(MainDestination.Settings) }
                ) { page ->
                    when (page) {
                        MainDestination.Home -> HomeScreen(
                            blockedSlots = model.uiState.blockedSlots,
                            nextLesson = model.uiState.nextLesson,
                            selectedDay = model.uiState.selectedDay,
                            dayLessons = model.uiState.dayLessons,
                            onSlotClick = model::selectDay,
                            onOpenLessonPage = { lesson, page -> LessonBrowserNavigator.open(this, lesson, page) }
                        )
                        MainDestination.Settings -> MenuScreen(
                            creditMessage = menuModel.creditMessage,
                            showCredits = menuModel.showCredits,
                            onOpenCredits = menuModel::openCredits,
                            onCloseCredits = menuModel::closeCredits,
                            onMoodle = { startActivity(Intent(this, MoodleImportActivity::class.java)) },
                            onTimetable = { startActivity(Intent(this, TimetableEditorActivity::class.java)) },
                            onTimeSettings = { startActivity(Intent(this, LessonTimeEditorActivity::class.java)) },
                            onNotificationSettings = { startActivity(Intent(this, NotificationSettingsActivity::class.java)) },
                            onNotificationTest = { startActivity(Intent(this, NotificationTestActivity::class.java)) },
                            onThemeSettings = { startActivity(Intent(this, ThemeEditorActivity::class.java)) },
                            paused = model.uiState.paused,
                            onTogglePause = model::togglePause
                        )
                    }
                }
            }
        }
        // 許可の判定は処理側、設定画面を開く操作は画面側が担当する。
        if (!model.hasNotificationAccess()) PermissionSettingsNavigator.openNotificationAccess(this)
        if (!model.canScheduleAlarms()) PermissionSettingsNavigator.openExactAlarmAccess(this)
    }

    override fun onResume() {
        super.onResume()
        // 編集や端末設定から戻ったときに、最新の選択枠・一時停止を表示する。
        model.refresh()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectDestination(restoredDestination(intent.getStringExtra(EXTRA_DESTINATION)))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_DESTINATION, destination.name)
        outState.putBoolean("show_credits", menuModel.showCredits)
        super.onSaveInstanceState(outState)
    }

    private fun selectDestination(next: MainDestination) {
        // 設定タブへ移るときも、停止期限が過ぎていないか確認する。
        model.refresh()
        destination = next
    }

    private fun restoredDestination(name: String?): MainDestination =
        MainDestination.entries.firstOrNull { it.name == name } ?: MainDestination.Home

    companion object {
        const val EXTRA_DESTINATION = "main_destination"
    }
}

// 2. ホーム画面のCompose表示
/** 次の授業と時間割を表示し、押した曜日の全授業を時間割の下に並べる。 */
@Composable
fun HomeScreen(
    blockedSlots: Set<String>,
    nextLesson: NextLesson? = null,
    selectedDay: Int? = null,
    dayLessons: List<TimetableLesson> = emptyList(),
    onSlotClick: (String) -> Unit = {},
    onOpenLessonPage: (TimetableLesson, CoursePage) -> Unit = { _, _ -> }
) {
    val dayLessonsRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selectedDay) {
        if (selectedDay != null) {
            // 一覧を配置してからスクロールし、画面の下に追加された授業を見える位置へ出す。
            withFrameNanos { }
            dayLessonsRequester.bringIntoView()
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NextLessonCard(nextLesson)
        Text(
            "時間割をタップすると、その曜日の授業を表示します",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TimetableGrid(blockedSlots, onSlotClick = onSlotClick)
        if (selectedDay != null) {
            DayLessonsCard(
                selectedDay, dayLessons, Modifier.bringIntoViewRequester(dayLessonsRequester), onOpenLessonPage
            )
        }
    }
}

@Composable
private fun DayLessonsCard(
    day: Int,
    lessons: List<TimetableLesson>,
    modifier: Modifier = Modifier,
    onOpenLessonPage: (TimetableLesson, CoursePage) -> Unit = { _, _ -> }
) {
    val dayLabel = when (day) {
        Calendar.MONDAY -> "月"
        Calendar.TUESDAY -> "火"
        Calendar.WEDNESDAY -> "水"
        Calendar.THURSDAY -> "木"
        Calendar.FRIDAY -> "金"
        else -> return
    }
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("${dayLabel}曜日の授業", style = MaterialTheme.typography.titleMedium)
            if (lessons.isEmpty()) {
                Text("この曜日に登録された授業はありません", style = MaterialTheme.typography.bodyLarge)
            } else {
                lessons.forEachIndexed { index, lesson ->
                    if (index > 0) HorizontalDivider()
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${lesson.slot.lesson}限　${LessonTimeRules.formatTime(lesson.startTime)} 〜 ${LessonTimeRules.formatTime(lesson.endTime)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            lesson.subject?.takeIf { it.isNotBlank() } ?: "教科未設定",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onOpenLessonPage(lesson, CoursePage.MOODLE) }) {
                                Text("Moodle")
                            }
                            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { onOpenLessonPage(lesson, CoursePage.SYLLABUS) }) {
                                Text("シラバス")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NextLessonCard(nextLesson: NextLesson?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("次の授業", style = MaterialTheme.typography.titleMedium)
            if (nextLesson == null) {
                Text("授業が登録されていません", style = MaterialTheme.typography.bodyLarge)
                Text("設定から時間割を登録してください", style = MaterialTheme.typography.bodyMedium)
            } else {
                val date = Date(nextLesson.startsAt)
                Text(
                    "${nextLesson.slot.lesson}限　${SimpleDateFormat("HH:mm", Locale.JAPAN).format(date)} 〜 ${LessonTimeRules.formatTime(nextLesson.endTime)}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    nextLesson.subject?.takeIf { it.isNotBlank() } ?: "教科未設定",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// 3. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "ホーム・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "ホーム・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    AppScreen {
        MainScreenScaffold(destination = MainDestination.Home, onHome = {}, onSettings = {}) {
            HomeScreen(
                blockedSlots = setOf("MON_1", "TUE_3", "FRI_5"),
                nextLesson = NextLesson(
                    slot = TimetableSlot("MON_1", Calendar.MONDAY, 1),
                    startsAt = Calendar.getInstance().apply {
                        clear()
                        set(2026, Calendar.OCTOBER, 26, 8, 40)
                    }.timeInMillis,
                    endTime = LessonTime(9, 30),
                    subject = "情報処理"
                )
            )
        }
    }
}

@Preview(name = "曜日別授業・ライト", showBackground = true, widthDp = 360, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "曜日別授業・ダーク", showBackground = true, widthDp = 360, heightDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayLessonsPreview() {
    AppScreen {
        DayLessonsCard(
            day = Calendar.MONDAY,
            lessons = listOf(
                TimetableLesson(TimetableSlot("MON_1", Calendar.MONDAY, 1), LessonTime(8, 40), LessonTime(9, 30), "情報処理"),
                TimetableLesson(TimetableSlot("MON_3", Calendar.MONDAY, 3), LessonTime(10, 45), LessonTime(11, 35), "数学"),
                TimetableLesson(TimetableSlot("MON_5", Calendar.MONDAY, 5), LessonTime(13, 25), LessonTime(14, 15), "英語")
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

