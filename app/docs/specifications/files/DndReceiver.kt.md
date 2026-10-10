# DndReceiver.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/DndReceiver.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/DndReceiver.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割と起動元

BroadcastReceiverを継承し、曜日・時限の開始／終了アラームを受け取る。初回登録元はTimetableActivity、MainActivity2、BootReceiver。Manifestのexportedはfalse。

| 定数 | 値 | 用途 |
| --- | --- | --- |
| ACTION_DND_ON | com.example.super_kemo_taro3.DND_ON | ミュート開始 |
| ACTION_DND_OFF | com.example.super_kemo_taro3.DND_OFF | ミュート解除 |

## onReceive

1. 受信actionをDNDタグでログに出す。
2. NotificationManagerの通知ポリシーアクセス権限がなければ終了する。この場合は翌週分も登録しない。
3. timetable_settings / pause_until_sunday_timeを読み、現在時刻が期限前なら停止中と判定する。
4. ONなら、停止中でない場合にsetInterruptionFilter(NONE)を呼ぶ。
5. OFFなら、停止中でない場合にsetInterruptionFilter(ALL)を呼ぶ。
6. 認識したON・OFFについて、停止中でもrescheduleで次週分を予約する。

不明なactionは切替・再登録を行わない。blocked_slotsは読まず、受信したactionと停止期限を判断材料にする。

## reschedule(context, intent, action)

API 31以上で正確なアラームを利用できなければ再登録だけを終了する。元IntentのrequestCode:Int（既定0）とscheduledTime:Long（既定現在時刻）を読む。

次回日時はscheduledTime + AlarmManager.INTERVAL_DAY × 7。実際の受信日時に7日を加える処理ではない。同じaction・requestCodeを持つ明示Intentに次回日時を設定し、FLAG_UPDATE_CURRENT | FLAG_IMMUTABLEのPendingIntentでRTC_WAKEUPのsetExactAndAllowWhileIdleを実行する。

## 境界条件

通知フィルター解除はALLへの変更要求で、開始前のフィルターへの復元ではない。一時停止はON・OFFの両方の変更を抑止するが、正確なアラームの権限があれば予約は継続する。

繰返しは固定の7日分のミリ秒を加算する。タイムゾーン変更・夏時間変更時にCalendarで時刻を再計算する処理はない。長時間遅延して次回日時も過去になった場合、さらに未来まで週を繰り上げる処理はない。通知権限が失われた場合の再試行処理もない。

