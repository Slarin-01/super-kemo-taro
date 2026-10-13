# TimetableEditorPage.kt：曜日・時限の選択画面

更新日: 2026-10-13  
対象: [TimetableEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/TimetableEditorPage.kt)

## 役割

通知をミュートする曜日・時限を、表の枠を押して選ぶ画面です。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `TimetableEditorActivity` | 枠を押した操作を状態管理へ渡し、必要な許可の設定画面を開きます。 |
| `TimetableEditorScreen` | 操作できる時間割と「メニューに戻る」を表示します。 |
| `TimetableEditorScreenPreview` | ライト・ダークの表示例をAndroid Studioへ渡します。 |

## 扱う情報と動き

- 入力は、選択済みの枠名の一覧です。月〜金の1〜8限、合計40枠を表示します。
- 押すたびに選択・解除を切り替え、状態管理を通じてその場で保存します。
- 予約用の許可が不足していれば、選択を保存した後に端末設定を案内します。
- 設定画面から戻ると選択を読み直し、「戻る」でこの画面を閉じます。
- 現在のテスト設定では、表示が「金」の枠は内部で日曜日に動きます。曜日の変換は処理側の担当です。

状態管理: [TimetableState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/TimetableState.kt)  
共通の表: [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)  
曜日の変換・予約: [TimetableScheduling.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/TimetableScheduling.kt)

## 変更したいとき

編集画面の配置や戻るボタンを変えるときに編集します。
プレビューは表示確認用で、枠を押しても保存や予約は行いません。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
