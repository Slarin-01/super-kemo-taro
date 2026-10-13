# NotificationTestPage.kt：通知の切り替えテスト画面

更新日: 2026-10-13  
対象: [NotificationTestPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/NotificationTestPage.kt)

## 役割

通知のミュート・解除を手動で確認する画面です。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `NotificationTestActivity` | 状態管理と画面をつなぎ、許可不足なら端末設定を開きます。 |
| `NotificationTestScreen` | 切り替えボタン、現在の状態、戻るボタンを表示します。 |
| `NotificationTestScreenPreview` | ライト・ダークの表示例をAndroid Studioへ渡します。 |

## 扱う情報と動き

- 状態管理から受け取った文章を表示します。「ON」はミュート、「OFF」は解除を表します。
- ボタン操作を状態管理へ渡し、切り替えの結果を表示します。
- 状態管理から「許可の案内が必要」と返された場合は、端末設定を開きます。
- 端末設定から戻ると実際の通知状態を再確認し、「戻る」でこの画面を閉じます。

状態・結果の文章: [NotificationTestState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/NotificationTestState.kt)  
許可案内: [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)

## 変更したいとき

タイトル、ボタン、配置を変えるときに編集します。
プレビューでは固定の状態を表示し、通知を切り替えません。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
