# TimeSettings.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/TimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/TimeSettings.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

UI・Androidの保存機能に依存しない時刻データと検証処理を提供する。MainActivity2とTimeSettingsScreenから使用し、TimeSettingsTestで検証する。

## 定義と関数

| 名前 | 入出力・処理 |
| --- | --- |
| TimeSetting | saveKey:String、defaultHour:Int、defaultMinute:Intを持つdata class |
| defaultTime | formatTimeで初期時刻を文字列化する計算プロパティ |
| timeSettings | 各時限の開始・終了を交互に並べた16個のTimeSetting |
| formatTime(hour, minute) | Locale.ROOTで半角数字のHH:mmを返す。引数の範囲は検証しない |
| timeToMinutes(value) | コロンで分割した2要素を整数化し、時0〜23・分0〜59なら0時からの経過分を返す。不正ならnull |
| invalidTimeIndices(times) | 不正な入力欄の0始まり添字をSet<Int>で返す |

## 保存キーと初期時刻

| 時限 | 開始キー・初期値 | 終了キー・初期値 |
| --- | --- | --- |
| 1 | time_start_1 / 08:40 | time_end_1 / 09:30 |
| 2 | time_start_2 / 09:40 | time_end_2 / 10:30 |
| 3 | time_start_3 / 10:45 | time_end_3 / 11:35 |
| 4 | time_start_4 / 11:45 | time_end_4 / 12:35 |
| 5 | time_start_5 / 13:15 | time_end_5 / 14:25 |
| 6 | time_start_6 / 14:25 | time_end_6 / 15:15 |
| 7 | time_start_7 / 15:30 | time_end_7 / 16:20 |
| 8 | time_start_8 / 16:30 | time_end_8 / 17:20 |

添字2×(時限−1)は開始、次の添字は終了。TimetableActivity・BootReceiverの未保存時の5限は13:25〜14:15であり、この定義とは異なる。

## 検証規則

1. 開始または終了が時刻に変換できなければ、その欄をエラーにする。
2. 開始 >= 終了なら終了欄をエラーにする。日付をまたぐ時間帯も許可しない。
3. 直前の時限の終了 > 現在の開始なら現在の開始欄をエラーにする。
4. 直前の終了と現在の開始が同時刻なら許可する。

timeToMinutesは固定の文字数までは検証しないため、8:5も解析できる。保存用のformatTimeでは08:05になる。invalidTimeIndicesはリストを2要素ずつ処理し、16要素であること自体は検証しない。奇数要素なら存在しない終了欄の添字もエラー集合へ追加する。前の終了が不正な場合は、その区間との重複比較を行わない。

