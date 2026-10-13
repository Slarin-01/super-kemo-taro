# SharedScreenComponents.kt：画面で共通して使う部品

更新日: 2026-10-13  
対象: [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)

## 役割

画面全体の見た目、時間割、確認画面、表示設定と許可案内をまとめています。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `BellTheme`・`AppScreen` | 明暗の配色、背景、システムバーやキーボードを避ける余白を用意します。 |
| `TimetableGrid` | 月〜金の40枠を、選択済みは緑、未選択は灰色で表示します。 |
| `ConfirmationDialog` | 本文、確定ボタン、必要に応じてキャンセルボタンを表示します。 |
| `AppThemeController` | 現在の明暗を調べ、選ばれた表示テーマを適用します。 |
| `PermissionSettingsNavigator` | 通知制御・正確なアラームの許可を変更する端末設定を開きます。 |

## 扱う情報と動き

- 時間割は選択済みの枠名を受け取ります。クリック先を渡した場合は編集用、省略した場合は閲覧用です。
- 押した枠は `MON_1` などの名前で画面へ返します。実際の曜日の変換や予約は行いません。
- 確認画面は文章と操作を受け取り、押されたボタンの操作を呼び出します。
- テーマが未設定なら端末の表示設定を使います。アラームの許可案内はAndroid 12以降で開きます。
- 時間割は読み上げ機能にも曜日・時限・選択状態を伝えます。

共通部品は各画面ファイルから使います。通知の実際の制御は [NotificationControl.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationControl.kt)、予約は [AlarmScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/AlarmScheduling.kt) が担当します。

## 変更したいとき

共通の配色、時間割の色やサイズ、確認画面、許可案内を変えるときに編集します。
ライト・ダークの時間割プレビューは固定の例を表示し、保存や予約は行いません。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
