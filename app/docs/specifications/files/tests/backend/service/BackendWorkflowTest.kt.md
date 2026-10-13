# BackendWorkflowTest.kt：保存と通知予約のテスト

更新日: 2026-10-13  
対象: [BackendWorkflowTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/backend/service/BackendWorkflowTest.kt)

## 役割

選択、解除、時刻保存、一時停止、権限不足、再起動後の予約を確認します。

## 主な動き

- 金曜日の選択が日曜日の予約になることも確認します。確認項目は14件です。

## 変更したいとき

対応する機能のルールを変えたら、確認する内容も合わせて見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| 選択・時刻保存の確認 | 選択の保存、予約・取消、5限の初期値の違いを確認する |
| 移行・再起動の確認 | 旧予約の移行と保存した枠の復元を確認する |
| 停止・権限の確認 | 通知変更と次週予約の実行条件を確認する |

## 扱う情報と結果

設定・通知・予約の代役と固定した時計を使います。14件のテストで、保存値と依頼の内容が期待どおりか確認します。

関連する場所: [時間割と予約の連携](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt) / [停止と受信後の判断](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationAutomation.kt)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

