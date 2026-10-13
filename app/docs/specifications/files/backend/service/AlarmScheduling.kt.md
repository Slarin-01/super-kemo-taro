# AlarmScheduling.kt：Androidへの時刻予約

更新日: 2026-10-13  
対象: [AlarmScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/AlarmScheduling.kt)

## 役割

授業の開始・終了時刻に知らせてもらうため、Androidへ予約を登録・取消します。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `AlarmContract` | 開始・終了の知らせの名前と、枠ごとの予約番号を決めます。 |
| `AlarmScheduler` | 予約、取消、許可確認の窓口を定義します。 |
| `AndroidAlarmScheduler.scheduleSlot` | 1枠について、開始用と終了用の2件を予約します。 |
| `cancelSlot・cancelLegacySlot` | 現在の予約、または以前の受信先に向けた予約を取り消します。 |
| `scheduleNext` | 元の予定日時の7日後を予約します。受信の遅れを次回へ持ち越しません。 |

## 扱うデータと結果

入力は、枠の名前、実際に動かす曜日、開始・終了の時刻です。Androidへ渡す予約には、開始か終了か、予約番号、予定日時を入れます。

同じ受信先・知らせの名前・予約番号で予約を識別します。再登録では同じ予約を更新し、取消では存在する予約だけを探します。

## 現在の動き・設定

- 正確な時刻で予約する許可は、呼び出し側が確認してから登録を依頼します。
- 開始と終了の次回日時は別々に計算します。授業の途中で登録すると、開始だけが翌週になる場合があります。
- 次回予約は固定の7日後です。旧受信先の名前は取消のためだけに残しています。

## つながり・変更したいとき

[TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)などから依頼を受け、予約時刻には[AlarmReceivers.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/receiver/AlarmReceivers.kt)へ知らせが届きます。Androidへの予約方法や取消方法を変えるときに編集します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
