/*
 * フロントエンド・状態管理: Moodleから履修科目を取り込み、ミュート枠を置き換えるまでの表示状態。
 * 通信と保存はサービスへ渡し、このクラスは進行と結果の文面だけを持つ。
 */
package com.example.super_kemo_taro3.frontend.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.super_kemo_taro3.backend.repository.MoodleLogin
import com.example.super_kemo_taro3.backend.repository.SettingsRepository
import com.example.super_kemo_taro3.backend.service.HttpPages
import com.example.super_kemo_taro3.backend.service.ImportFailed
import com.example.super_kemo_taro3.backend.service.ImportResult
import com.example.super_kemo_taro3.backend.service.MoodleImport
import com.example.super_kemo_taro3.backend.service.TimetableService
import java.io.IOException
import java.security.SecureRandom

// 1. 取り込みの進行
/** 確認、ブラウザ接続、取得中、結果を切り替える。ブラウザへ開くURLが必要なときだけ文字列を返す。 */
class MoodleImportViewModel(
    private val settings: SettingsRepository,
    private val timetable: TimetableService
) : ViewModel() {
    var phase by mutableStateOf(Phase.Confirm)
        private set
    var message by mutableStateOf(confirmMessage)
        private set
    var progress by mutableStateOf<Float?>(null)
        private set
    var onNeedsAlarmPermission: () -> Unit = {}
    private var openAlarmSettings = false
    private var acceptedUrl: String? = null

    fun launchUrl(): String? {
        if (phase == Phase.Importing) return null
        val saved = settings.readMoodleToken()
        if (saved != null) {
            runImport(saved)
            return null
        }
        val passport = ByteArray(16).also { SecureRandom().nextBytes(it) }
            .joinToString("") { "%02x".format(it) }
        settings.writeMoodleLogin(MoodleLogin(MoodleImport.passportSiteId(passport), System.currentTimeMillis() + 15 * 60 * 1000))
        phase = Phase.Waiting
        message = "ブラウザで岡大IDに接続しています。"
        return MoodleImport.launchUrl(passport)
    }

    fun acceptToken(url: String) {
        if (url == acceptedUrl || phase == Phase.Importing) return
        acceptedUrl = url
        val returned = MoodleImport.returnedToken(url)
        val login = settings.readMoodleLogin()
        settings.writeMoodleLogin(null)
        if (returned == null || login == null || login.expiresAt < System.currentTimeMillis() || login.siteId != returned.siteId) {
            phase = Phase.Done
            message = "接続の有効期限が切れたか、別の接続結果です。取り込みを開始し直してください。"
            return
        }
        settings.writeMoodleToken(returned.token)
        runImport(returned.token)
    }

    fun fail(text: String) {
        phase = Phase.Done
        message = text
    }

    fun retry() {
        acceptedUrl = null
        progress = null
        phase = Phase.Confirm
        message = confirmMessage
    }

    fun consumeAlarmSettings(): Boolean {
        val open = openAlarmSettings
        openAlarmSettings = false
        return open
    }

    private fun runImport(token: String) {
        phase = Phase.Importing
        progress = null
        message = "履修科目を取り込んでいます。"
        val main = Handler(Looper.getMainLooper())
        Thread {
            val outcome = try {
                val now = System.currentTimeMillis()
                val result = MoodleImport.collect(
                    token,
                    MoodleImport.tokyoToday(now),
                    now / 1000,
                    HttpPages()
                ) { done, total ->
                    main.post {
                        if (phase != Phase.Importing) return@post
                        progress = if (total == 0) 0f else done.toFloat() / total
                        message = "履修科目を取り込んでいます。（$done/$total）"
                    }
                }
                Outcome(resultText(result), timetable.replaceSelectedSlots(result.slots), false)
            } catch (error: ImportFailed) {
                Outcome(error.message ?: "取り込みできません", false, error.unauthorized)
            } catch (_: IOException) {
                Outcome("大学の公開情報を取得できません", false, false)
            }
            main.post {
                if (outcome.unauthorized) settings.writeMoodleToken(null)
                message = outcome.text
                phase = Phase.Done
                if (outcome.needsPermission) {
                    openAlarmSettings = true
                    onNeedsAlarmPermission()
                }
            }
        }.start()
    }

    private fun resultText(result: ImportResult): String {
        val skipped = if (result.unmatched == 0) "" else "時間割に入らない科目が${result.unmatched}件あります。"
        return "${result.slots.size}枠を取り込みました。$skipped"
    }

    private data class Outcome(val text: String, val needsPermission: Boolean, val unauthorized: Boolean)

    enum class Phase { Confirm, Waiting, Importing, Done }

    private companion object {
        const val confirmMessage = "岡大Moodleの履修科目で、ミュートする枠を置き換えます。今の選択は残しません。"
    }
}
