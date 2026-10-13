# LessonTimeEditorPage.kt：授業時刻の編集画面

更新日: 2026-10-13  
対象: [LessonTimeEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/LessonTimeEditorPage.kt)

## 役割

1〜8限の開始・終了時刻を入力し、保存・初期化・破棄を確認する画面です。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `LessonTimeEditorActivity` | 編集状態を接続・復元し、保存結果に応じて許可案内やホームへの移動を行います。 |
| `LessonTimeEditorScreen` | 8行の時刻欄、エラー印、操作ボタン、確認画面を表示します。 |
| `TimeField` | 1個の時刻と、必要な場合は赤い「!」を表示します。 |
| `LessonTimeDialog` | 時計・キーボードで時刻を入力し、「OK」で時・分を返します。 |

## 扱う情報と動き

- 状態管理から16時刻を受け取ります。順番は「1限開始、1限終了、2限開始、2限終了…」です。
- 選んだ欄とエラーの場所は、この16時刻の番号で扱います。
- 入力中の時刻は、入力画面の「OK」で編集一覧へ反映します。「保存する」の確認までは設定を保存しません。
- 保存結果が許可不足なら端末設定を案内し、その後ホームへ戻ります。
- Androidが画面を作り直す場合に備え、編集一覧・選択欄・確認画面を復元用データに残します。

編集状態と保存判断: [LessonTimeEditorState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/LessonTimeEditorState.kt)  
共通の確認画面・許可案内: [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)

## 変更したいとき

時刻欄、入力画面、確認メッセージの見た目を変えるときに編集します。
ライト・ダークのプレビューは16時刻の例を表示し、保存や通知制御は行いません。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
