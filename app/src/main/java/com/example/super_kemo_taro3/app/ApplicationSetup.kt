/*
 * 起動・接続: Applicationの初期化と、画面・受信処理で共用するサービスの接続。
 * 各サービスを一組作り、画面と受信入口で共用する。起動時に保存された表示テーマを適用する。
 */
package com.example.super_kemo_taro3.app

import android.app.Application
import android.content.Context
import com.example.super_kemo_taro3.backend.repository.SettingsRepository
import com.example.super_kemo_taro3.backend.repository.SharedPreferencesSettingsRepository
import com.example.super_kemo_taro3.backend.service.AlarmDeliveryService
import com.example.super_kemo_taro3.backend.service.AndroidAlarmScheduler
import com.example.super_kemo_taro3.backend.service.AndroidNotificationGateway
import com.example.super_kemo_taro3.backend.service.LessonTimeService
import com.example.super_kemo_taro3.backend.service.NotificationModeService
import com.example.super_kemo_taro3.backend.service.PauseService
import com.example.super_kemo_taro3.backend.service.TimetableService
import com.example.super_kemo_taro3.frontend.screens.AppThemeController
import com.example.super_kemo_taro3.frontend.viewmodel.AppearanceState

// 1. アプリ起動と保存テーマの適用
/** 全画面・受信処理が使うサービスを保持し、起動時に保存テーマを適用する。Manifestに登録するApplication。 */
class BellApplication : Application() {
    // 起動中に一度だけ生成し、画面を開き直しても同じ処理の組を使う。
    val services: AppServices by lazy { AppServices(this) }

    override fun onCreate() {
        super.onCreate()
        AppThemeController.apply(services.settings.readDarkMode())
    }
}

// 2. 設定・通知・アラーム処理の接続
/** ApplicationのContextからRepository・Android API実装・各Serviceを1組作成し、画面とReceiverで共用する。 */
class AppServices(context: Context) {
    // 画面と受信処理は同じ保存・処理インスタンスを使用する。
    val settings: SettingsRepository = SharedPreferencesSettingsRepository(context)
    val appearance = AppearanceState(settings)
    private val alarms = AndroidAlarmScheduler(context)
    val notifications = NotificationModeService(AndroidNotificationGateway(context))
    val pause = PauseService(settings, notifications)
    val timetable = TimetableService(settings, alarms)
    val lessonTimes = LessonTimeService(settings, timetable)
    val alarmDelivery = AlarmDeliveryService(notifications, pause, alarms, settings)
}

/** ActivityやReceiverのContextから共通処理を取得する。ManifestにBellApplicationが登録されていることが前提。 */
val Context.appServices: AppServices
    get() = (applicationContext as BellApplication).services

