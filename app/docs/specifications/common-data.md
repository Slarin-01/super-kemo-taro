# 共通データ・処理仕様

確認日: 2026-10-11。現在のappモジュールの実装を整理した資料。

## 画面と担当

| 画面 | Activity | Compose UI |
| --- | --- | --- |
| ホーム | MainActivity | AppScreen / MainScreen / TimetableGrid |
| メニュー | MainActivity3 | AppScreen / MenuScreen |
| 曜日・時限の選択 | TimetableActivity | AppScreen / TimetableScreen / TimetableGrid |
| 時限カスタム | MainActivity2 | AppScreen / TimeSettingsScreen |
| 通知切り替えテスト | NotificationTestActivity | AppScreen / NotificationTestScreen |

5画面はActivityのsetContentで表示する。画面レイアウトのXMLファイルはなく、res内のXMLはテーマ・色・文字列・アイコン・バックアップ設定用。

## 主な呼び出しの流れ

ホームの「編集」 → MainActivity3のメニュー → 各編集・テストActivity。

曜日・時限を選択 → TimetableActivityが選択を保存・アラーム予約 → DndReceiverが通知フィルター変更・翌週予約。

時刻を保存 → MainActivity2が16時刻を保存・全選択枠を再予約 → DndReceiverへ。

端末再起動 → BootReceiverが保存済み枠・時刻から再予約 → DndReceiverへ。

## SharedPreferences

すべてMODE_PRIVATEを使用する。

| 保存領域 | キー・型 | 既定値 | 書き込み元 | 主な読み込み元 |
| --- | --- | --- | --- | --- |
| timetable_settings | blocked_slots / StringSet | 空集合 | TimetableActivity | MainActivity・TimetableActivity・MainActivity2・BootReceiver |
| timetable_settings | pause_until_sunday_time / Long | 0 | MainActivity | MainActivity・DndReceiver |
| timetable_prefs | time_start_1〜time_start_8 / String | 読み込み元の初期値 | MainActivity2 | MainActivity2・TimetableActivity・BootReceiver |
| timetable_prefs | time_end_1〜time_end_8 / String | 読み込み元の初期値 | MainActivity2 | MainActivity2・TimetableActivity・BootReceiver |
| theme_prefs | is_dark_mode / Boolean | キーなしならApplicationは上書きしない | MainActivity | SuperKemoTaroApp |

MainActivity2のreschedulAllAlarmsもtimetable_prefsを読むが、時刻キーがない枠は初期値で補わず読み飛ばす。表示・編集時刻にはTimeSettings.ktの初期値を使う。

時刻は保存時に半角のHH:mm文字列へ整形する。変更はSharedPreferences.Editor.applyで保存する。個々のキー名を変える場合は、読み込み元・書き込み元を合わせて確認する必要がある。

## 曜日・時限のキー

文字列の形式は「曜日キー_時限番号」。例: MON_1、FRI_8。UIが生成するのはMON・TUE・WED・THU・FRIと1〜8の組合せ40個。blocked_slotsは選択済みキーの集合であり、曜日だけの単独設定はない。

UIではFRIを金曜日として表示する。テスト用に、TimetableActivity・MainActivity2・BootReceiverのすべてでFRIを日曜日に登録する。表示・保存キー・アラーム識別子は維持し、内部の曜日変換だけを変更する。[現実装の注意点](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/implementation-notes.md)に反映手順を記載する。

## 時限の初期値

| 時限 | TimeSettings.kt（編集・初期化） | TimetableActivity / BootReceiver（未保存時の予約） |
| --- | --- | --- |
| 1 | 08:40〜09:30 | 08:40〜09:30 |
| 2 | 09:40〜10:30 | 09:40〜10:30 |
| 3 | 10:45〜11:35 | 10:45〜11:35 |
| 4 | 11:45〜12:35 | 11:45〜12:35 |
| 5 | **13:15〜14:25** | **13:25〜14:15** |
| 6 | 14:25〜15:15 | 14:25〜15:15 |
| 7 | 15:30〜16:20 | 15:30〜16:20 |
| 8 | 16:30〜17:20 | 16:30〜17:20 |

カスタム保存後の予約は保存値を優先する。初期化操作だけでは保存しないため、編集画面で初期化後に保存を確定するまで既存の予約は更新されない。

## アラームの契約

| 項目 | 開始 | 終了 |
| --- | --- | --- |
| 受信先 | DndReceiver | DndReceiver |
| action | com.example.super_kemo_taro3.DND_ON | com.example.super_kemo_taro3.DND_OFF |
| requestCode | slotKey.hashCode() | slotKey.hashCode() + 1000 |
| Intent extra requestCode | 同じInt | 同じInt |
| Intent extra scheduledTime | 予定日時のLong（エポックミリ秒） | 同左 |
| PendingIntent | getBroadcast / FLAG_UPDATE_CURRENT &#124; FLAG_IMMUTABLE | 同左 |
| AlarmManager | RTC_WAKEUP / setExactAndAllowWhileIdle | 同左 |
| 通知フィルター要求 | NONE | ALL |

ONはミュート開始、OFFは通知許可側への変更要求を意味する。同じ枠の更新・取消では同じ受信先・action・requestCodeを使う。

初回・編集・再起動時の予約はCalendarで曜日・時刻を求める。開始・終了は個別に計算し、過去ならそれぞれ1週間進める。したがって時限の途中で初回登録すると、終了は今週、開始は翌週になる場合がある。現在時刻が時限内かを判定して即時にミュートする処理はない。

DndReceiverは受信後、scheduledTimeに固定の7日分を加えて次回を登録する。受信遅延を次回日時に加算しない。

## 一時停止

停止期限は直近の日曜日の23:59:59.999。現在時刻 < 期限なら停止中。停止を有効にしたとき、権限があれば通知フィルターをALLへ変更する。

アラームは取消せず、DndReceiverが停止中のON・OFFを抑止する。通知・正確なアラームの権限があれば次週予約は続ける。期限経過後は自動的に停止解除判定になるが、期限経過そのものを契機に通知モードを変更する専用アラームはない。

## 権限確認の担当

| ファイル | 確認・不足時の動作 |
| --- | --- |
| MainActivity | 通知ポリシーアクセスと、API 31以上の正確なアラームを確認。端末設定を開く |
| TimetableActivity / MainActivity2 | API 31以上でcanScheduleExactAlarmsを確認。端末設定を開き、その登録処理を終了 |
| BootReceiver | API 31以上でUSE_EXACT_ALARMの許可またはcanScheduleExactAlarmsを確認。不可なら復元を終了 |
| DndReceiver | 通知権限なしなら切替も再予約も終了。正確なアラーム不可なら次回予約を終了 |
| NotificationTestActivity | 通知権限がなければ端末設定を開く |

端末の権限を許可した結果を受けて自動的に全アラームを再登録する共通処理はない。

