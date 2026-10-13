# LessonTimeSettings.kt：授業時刻のルールと保存

更新日: 2026-10-13  
対象: [LessonTimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/LessonTimeSettings.kt)

## 役割

授業の初期時刻、入力チェック、読み込み・保存を担当します。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `LessonTime・LessonTimeField` | 時・分と、各入力欄の保存先・初期時刻を表します。 |
| `LessonTimeDefaults` | 1〜8限の開始・終了に使う初期時刻を用意します。 |
| `LessonTimeRules` | 時刻を文字列に整え、入力の形式・順序・重なりを確認します。 |
| `LessonTimeService.loadTimes・defaultTimes` | 保存済みの時刻、または編集用の初期時刻を返します。 |
| `LessonTimeService.save・SaveTimesResult` | 問題がなければ保存・再予約を依頼し、結果を画面側へ返します。 |

## 扱うデータと結果

入力は「1限開始、1限終了、2限開始、2限終了…」の順に並ぶ16個の時刻文字列です。

- 時刻が不正、開始が終了以降、直前の時限と重なる場合は保存しません。時限同士の境目が同時刻なら保存できます。
- 正しい入力は16欄を保存してから、選択済みの枠を再予約します。
- 結果は「未保存」「保存・再予約済み」「保存済みだが予約の許可が必要」の3種類です。許可不足でも入力は保存されます。

## 現在の動き・設定

5限は、編集画面の初期値が13:15〜14:25、時刻未保存時の予約が13:25〜14:15です。現在の違いをそのまま残しています。

## つながり・変更したいとき

保存は[AppSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/repository/AppSettings.kt)、再予約は[TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)へ依頼します。初期時刻は`LessonTimeDefaults`、入力の条件は`LessonTimeRules`を編集します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
