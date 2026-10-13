# ScheduleCalculatorTest.kt：予約日時の計算テスト

更新日: 2026-10-13  
対象: [ScheduleCalculatorTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/backend/service/ScheduleCalculatorTest.kt)

## 役割

日曜日の予定、翌週への繰り越し、一時停止の期限などを確認します。

## 主な動き

- 確認項目は6件です。

## 変更したいとき

対応する機能のルールを変えたら、確認する内容も合わせて見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| 曜日と次回日時 | 金曜日キーが日曜日で動き、過去時刻は翌週になることを確認する |
| 停止期限 | 日曜日当日と月曜日からの停止期限を確認する |
| 予約の識別 | 不正な枠を除外し、予約の識別方法を維持することを確認する |

## 扱う情報と結果

固定した日付を使い、実行日に左右されず6件を確認します。

関連する場所: [曜日・日時の計算](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

