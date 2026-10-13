# NotificationAutomation.kt：一時停止と自動切り替え

更新日: 2026-10-13  
対象: [NotificationAutomation.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationAutomation.kt)

## 役割

一時停止を管理し、予約時刻になったときに通知を切り替えてよいか判断します。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `PauseService.isPaused` | 現在時刻が停止期限より前か確認します。 |
| `PauseService.toggle` | 一時停止の開始・解除を切り替えます。開始時は通知の解除も依頼します。 |
| `AlarmDeliveryService.handle` | 開始・終了の知らせ、通知の許可、停止状態を確認して切り替えます。 |
| `handle内の次回予約` | 予約の許可があれば、同じ知らせを翌週も受け取れるよう依頼します。 |

## 扱うデータと結果

一時停止では保存された期限と現在時刻を扱います。自動切り替えでは、受信した知らせの種類、予約番号、元の予定日時を受け取ります。

開始の知らせならミュート、終了なら解除を依頼します。対象外の知らせは何もしません。

## 現在の動き・設定

- 一時停止は次の日曜日末までです。日曜日当日はその日末まで止めます。期限後は設定値が残っていても停止解除として扱います。
- 停止中も予約は取り消しません。通知変更だけを止め、翌週の予約は続けます。停止を解除しても、その場でミュートし直す処理はありません。
- 通知を変更する許可がない場合は、通知変更と翌週の予約を行いません。
- 予約の許可だけがない場合は、今回の通知変更を行い、翌週の予約を見送ります。

## つながり・変更したいとき

[AlarmReceivers.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/receiver/AlarmReceivers.kt)から知らせを受け、[NotificationControl.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationControl.kt)と[AlarmScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/AlarmScheduling.kt)へ依頼します。一時停止や受信後の判断を変えるときに編集します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
