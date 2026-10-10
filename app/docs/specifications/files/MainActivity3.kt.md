# MainActivity3.kt 仕様書

対象: [src/main/java/com/example/super_kemo_taro3/MainActivity3.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/MainActivity3.kt)  
確認日: 2026-10-11  
パッケージ: `com.example.super_kemo_taro3`

## 役割

編集メニューのActivity。onCreateでedge-to-edgeを有効にし、AppScreen内にSecondaryScreens.ktのMenuScreenを表示する。独自の保存データはない。

## ボタンと遷移先

| 表示 | MenuScreenへ渡すコールバック | 起動先 |
| --- | --- | --- |
| 時間割を見る | onMain | MainActivity |
| 時刻設定 | onTimetable | TimetableActivity |
| 時限カスタム | onTimeSettings | MainActivity2 |
| 通知切り替えテスト | onNotificationTest | NotificationTestActivity |
| クレジット | MenuScreen内部で処理 | ダイアログ表示 |

各遷移は明示IntentのstartActivityで行い、MainActivity3自身はfinishしない。追加データはIntentへ渡さない。「時刻設定」の遷移先は曜日・時限の選択画面である。

## 定義

| 名前 | 内容 |
| --- | --- |
| onCreate | Compose画面と上記の画面遷移コールバックを設定 |
| creditMessage | クレジットダイアログ用の固定文字列。trimIndentで字下げを除去 |

creditMessageにはアプリ名「言十ベル」、バージョン1.0、グループ「スーパーKEMOたろう」、メンバー「スラリン・ペパチキ・くぬたろう」、使用ツール、NotificationManager等のAPI、対応OS「Android 7.0以上」を記載する。

## 保守上の注意

クレジット内のバージョンと対応OSは固定文字列で、build.gradle.ktsから自動生成されない。バージョン・最小SDKを変更した場合は本文も確認する。ダイアログの開閉状態はMenuScreenが保持する。

