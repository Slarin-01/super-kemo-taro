# AlarmReceivers.kt：Androidからの知らせを受け取る部分

更新日: 2026-10-13  
対象: [AlarmReceivers.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/receiver/AlarmReceivers.kt)

## 役割

予約時刻、端末再起動、アプリ更新の知らせを受け取る入口です。通知を切り替える判断や予約の計算は、処理側へ渡します。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `NotificationAlarmReceiver.onReceive` | アラームの情報を受け取り、通知切り替えの処理へ渡します。 |
| `AlarmRecoveryReceiver.onReceive` | 再起動・アプリ更新を受け取り、保存枠からの予約復元を依頼します。 |

## 扱うデータと結果

アラームでは、開始・終了を示す知らせの名前、予約番号、元の予定日時を受け取ります。番号がない場合は`0`、予定日時がない場合は現在時刻を渡します。

復元では、端末再起動（`BOOT_COMPLETED`）とアプリ更新（`MY_PACKAGE_REPLACED`）を扱います。予約の許可不足で復元できない場合は記録を残します。

## つながり・変更したいとき

アラームは[NotificationAutomation.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationAutomation.kt)、復元は[TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)へ依頼します。

受け取る知らせや渡し先を変えるときに編集します。Androidへの受信登録は[AndroidManifest.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml)にもあります。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
