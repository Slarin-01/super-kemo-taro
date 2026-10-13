# TimetableScheduling.kt：曜日・時限と予約のまとめ役

更新日: 2026-10-13  
対象: [TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)

## 役割

選んだ曜日・時限を、通知を切り替える予約につなげます。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `TimetableSlot・TimetableRules.parseSlot` | MON_1などの名前を、動作曜日と1〜8限の番号に変換します。 |
| `ScheduleCalculator` | 次の予約日時と、一時停止する日曜日末の日時を計算します。 |
| `TimetableService.toggleSlot` | 枠を選択・解除し、保存と予約・取消を依頼します。 |
| `rescheduleSelectedSlots` | 時刻保存後に、選択した枠を保存済みの時刻で再予約します。 |
| `restoreAfterBoot・migrateReceiverIfNeeded` | 再起動・更新後の予約を復元し、旧受信先の予約を作り直します。 |

## 扱うデータと結果

入力は枠の名前と保存済みの授業時刻です。枠の選択・復元では、未保存の時刻や読み取れない部分を初期値で補います。不正な枠は予約しません。保存値だけで再予約するときは、読み取れない時刻の枠も飛ばします。

- `toggleSlot`の`true`は、予約の許可が不足していることを表します。許可がなくても選択した枠は保存します。
- 時刻保存後の再予約は保存値だけを使います。戻り値が`false`なら予約の許可が必要です。
- 旧予約の移行は一度だけ行います。許可不足の間は移行済みにせず、後で再試行できます。許可設定から戻るだけで通常の全枠再予約をする処理はありません。

## 現在の動き・設定

テスト用に、画面の「金曜日」を内部では日曜日として動かしています。表示と保存名は金曜日のままです。対応は`TimetableRules`の`FRI`で管理し、選択・時刻保存・再起動などすべての予約経路で使います。

日時は端末のタイムゾーンで計算し、すでに過ぎた予約日時は翌週にします。一時停止の期限は次の日曜日末（日曜日なら当日末）です。

## つながり・変更したいとき

設定は[AppSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/repository/AppSettings.kt)から読み、予約は[AlarmScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/AlarmScheduling.kt)へ依頼します。動作曜日は`TimetableRules`、予約を作り直す流れは`TimetableService`を変更します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
