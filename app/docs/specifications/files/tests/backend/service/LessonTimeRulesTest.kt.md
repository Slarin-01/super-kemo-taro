# LessonTimeRulesTest.kt：授業時刻の入力テスト

更新日: 2026-10-13  
対象: [LessonTimeRulesTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/backend/service/LessonTimeRulesTest.kt)

## 役割

時刻の読み取り、開始・終了の順序、時限の重なりを確認します。

## 主な動き

- 確認項目は6件です。

## 変更したいとき

対応する機能のルールを変えたら、確認する内容も合わせて見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| 初期値と時刻の読み取り | 16時刻の定義と正しい時刻文字列を確認する |
| 順序と重複の確認 | 開始・終了の逆転や時限の重なりを確認する |
| エラー欄の確認 | 修正すべき時刻欄に印が付くことを確認する |

## 扱う情報と結果

6件のテストで、正しい時刻は受け付け、不正な時刻は保存対象にしない規則を確認します。

関連する場所: [時刻のルール](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/LessonTimeSettings.kt)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

