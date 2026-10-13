# NotificationTestState.kt：通知テストの表示状態

更新日: 2026-10-13  
対象: [NotificationTestState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/NotificationTestState.kt)

## 役割

通知の現在の状態と、切り替え結果を画面に表示する文章へ変換します。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `NotificationTestViewModel`・`status` | 画面に表示する通知状態の文章を持ちます。 |
| `refresh` | 処理側へ現在の状態を確認し、表示文を更新します。 |
| `hasAccess` | 通知を制御する許可があるかを確認します。 |
| `toggle` | 通知を切り替え、結果と許可案内が必要かどうかを返します。 |

## 扱う情報と動き

- 処理側から受け取る `true` はミュート、`false` は解除、`null` は許可不足です。
- 表示文の「ON」はミュート、「OFF」は解除を表します。
- `toggle` の戻り値は別の意味で、`true` の場合に「許可設定を案内してほしい」と画面へ伝えます。
- 端末の通知操作は処理側、許可設定画面を開く操作は画面側が担当します。

通知の制御: [NotificationControl.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationControl.kt)  
表示・許可案内: [NotificationTestPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/NotificationTestPage.kt)

## 変更したいとき

状態メッセージや、切り替え結果の伝え方を変えるときに編集します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
