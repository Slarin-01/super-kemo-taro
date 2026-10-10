# TimeSettingsTest.kt 仕様書

対象: [src/test/java/com/example/super_kemo_taro3/TimeSettingsTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/TimeSettingsTest.kt)  
確認日: 2026-10-11

## 役割と実行環境

JUnit 4でTimeSettings.ktの純粋関数と初期値を検証する。開発PCで実行し、Android端末・エミュレーターは不要。プロジェクトルートのGradle Wrapperで :app:testDebugUnitTest を実行する。

## テストケース

| テストメソッド | 入力・期待する結果 |
| --- | --- |
| defaultsAreValid | timeSettingsの初期値にエラーがなく、先頭キーがtime_start_1、末尾キーがtime_end_8 |
| reversedAndZeroLengthRangesMarkEndField | 開始09:30、終了08:40または09:30で終了欄の添字1がエラー |
| overlappingPeriodsMarkFollowingStartField | 08:40〜09:30と09:20〜10:30で次の開始欄の添字2がエラー |
| adjacentPeriodsAreAllowed | 前の終了と次の開始が09:30で等しい場合にエラーなし |
| invalidSavedValuesCannotBeSavedAgain | 空文字、invalid、25:00、08:60、-1:30、08:40:00を解析できず、開始欄がエラー |
| timeFormattingAndDayBoundaries | formatTime(8,5)が08:05。00:00が0分、23:59が1439分 |

## 検証範囲

6個のテストメソッド。UIの操作、保存処理、曜日対応、AlarmManagerの実発火、通知権限、再起動復元は検証しない。TimetableActivityとTimeSettings.ktの初期値が同一であることも検証していない。

2026-10-11のFRIを日曜日に動かす変更後、:app:testDebugUnitTestで本クラスの6件が成功した。ExampleUnitTestの1件を含めて計7件成功、失敗・エラーは0件。曜日対応そのものや端末の実発火を検証した結果ではない。

