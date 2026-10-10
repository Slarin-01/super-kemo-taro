# BootReceiver.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/BootReceiver.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/BootReceiver.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

BroadcastReceiverを継承し、端末のBOOT_COMPLETED受信時に選択済みの時間割アラームを復元する。ManifestにRECEIVE_BOOT_COMPLETED権限とexported=trueのReceiverとして登録される。

## 定義

| 名前 | 内容 |
| --- | --- |
| slotTimes | 未保存時の1〜8限の開始・終了初期値 |
| dayMap | MON〜THUは月〜木、FRIはテスト用にCalendar.SUNDAYへ対応させる |
| onReceive(context, intent) | 起動完了actionだけを処理し、保存データからアラームを登録 |

slotTimesの5限は13:25〜14:15。他の時限は共通データ仕様書に記載した値。FRIはTimetableActivity・MainActivity2と同じく日曜日として登録し、再起動後もテスト用の曜日対応を維持する。

## 復元の流れ

1. actionがIntent.ACTION_BOOT_COMPLETEDでなければ終了する。
2. timetable_settings / blocked_slotsとtimetable_prefsの時刻を読む。
3. API 31以上ではUSE_EXACT_ALARMが許可済み、またはcanScheduleExactAlarmsがtrueでなければログを出して終了する。
4. 曜日・時限キーを分解し、不正な形式、未知の曜日、1〜8限以外の時限を読み飛ばす。
5. 保存時刻を優先し、未設定・数値化できない部分はslotTimesで補う。
6. 開始・終了それぞれの日時をCalendarで計算し、過去なら1週間進める。
7. DndReceiverへのON・OFFを共通のaction・requestCode・extrasで正確なアラームとして登録し、DNDタグでログを出す。

## 保存と境界条件

設定を読むだけで、SharedPreferencesは書き換えない。停止期限は復元判断に使わず、DndReceiverが受信時に判定する。通知ポリシーアクセス権限をこのファイルでは確認しない。

時刻の範囲検証はなく、時・分が数値ならCalendarへ渡す。権限不足で終了した後、端末設定を開いたり自動で再試行したりする処理はない。TIME_SET・TIMEZONE_CHANGEDなどの受信・再計算処理も定義されていない。

