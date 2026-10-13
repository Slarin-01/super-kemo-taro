# LessonTimeEditorState.kt：時刻編集の表示状態

更新日: 2026-10-13  
対象: [LessonTimeEditorState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/LessonTimeEditorState.kt)

## 役割

保存前の入力、エラー、開いている入力欄と確認画面を管理します。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `LessonTimeUiState` | 16時刻、エラーの場所、選択欄、入力の初期値、確認の種類を持ちます。 |
| `TimeEditorConfirmation`・`TimeEditorEffect` | 開く確認画面と、操作後に画面側が行うことを表します。 |
| `LessonTimeEditorViewModel` | 復元した編集値、または保存済みの時刻を読み込んで状態を用意します。 |
| `openTime`・`confirmTime` | 選んだ欄を開き、確定した時刻と入力エラーを一覧へ反映します。 |
| `requestConfirmation`・`confirmAction` | 保存・初期化・破棄の確認と、その結果を扱います。 |

## 扱う情報と動き

- 時刻は「1限開始、1限終了、2限開始、2限終了…」の順です。選択欄の `-1` は入力画面が閉じた状態です。
- 復元された編集一覧が16個なら優先します。復元する一覧がなければ保存済みの時刻を使います。
- 入力を直すたびに順番や重複を確認します。エラーがあれば保存確認の代わりに修正を案内します。
- 初期化は編集中の一覧だけを戻します。「保存する」の確認で処理側へ渡します。
- 保存成功・許可不足・破棄などの結果を返し、画面を移動する操作は画面側に任せます。

表示・復元: [LessonTimeEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/LessonTimeEditorPage.kt)  
時刻の初期値・確認・保存: [LessonTimeSettings.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/LessonTimeSettings.kt)

## 変更したいとき

入力の持ち方、エラーの反映、確認画面の切り替えを変えるときに編集します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
