# AppSettings.kt：設定の保存と読み込み

更新日: 2026-10-13  
対象: [AppSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/repository/AppSettings.kt)

## 役割

選択した曜日・時限、授業時刻、一時停止、表示モードを端末に保存します。画面や処理側は、保存先を直接扱わず、このファイルの窓口を使います。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `SettingsRepository` | 他のファイルが使う読み込み・保存の窓口を定義します。 |
| `SharedPreferencesSettingsRepository` | Androidの設定保存機能を使い、窓口の処理を実行します。 |
| `readBlockedSlots・writeBlockedSlots` | MON_1など、ミュートする枠の名前を読み書きします。 |
| `readTime・writeTimes` | 1〜8限の開始・終了時刻を読み書きします。 |
| `readPauseUntil・readDarkModeなど` | 停止期限、暗い表示の設定、旧予約の移行状況を読み書きします。 |

## 扱うデータと結果

- 選択枠と停止期限は`timetable_settings`、時刻は`timetable_prefs`、表示モードは`theme_prefs`に保存します。
- 時刻は`08:40`のような文字列です。未保存なら読み込み結果は`null`となり、処理側で初期値を使います。
- 停止期限に`null`を渡すと停止を解除します。表示モードの未設定も`null`で、端末の設定を引き継ぎます。
- 保存先の名前と従来のキーを維持しているため、以前の設定を読み込めます。

## つながり・変更したいとき

[ApplicationSetup.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/app/ApplicationSetup.kt)が共通の保存窓口を作り、状態管理とバックエンド処理へ渡します。保存項目を増やすときは窓口と実装の両方を変更します。保存先やキーの変更は、既存の設定を読み込めなくなる場合があります。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
