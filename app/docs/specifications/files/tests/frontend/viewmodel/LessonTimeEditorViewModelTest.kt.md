# LessonTimeEditorViewModelTest.kt：時刻編集の状態テスト

更新日: 2026-10-13  
対象: [LessonTimeEditorViewModelTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/frontend/viewmodel/LessonTimeEditorViewModelTest.kt)

## 役割

未保存の編集、初期化、エラー、保存、破棄、画面復元を確認します。

## 主な動き

- 確認項目は6件です。

## 変更したいとき

対応する機能のルールを変えたら、確認する内容も合わせて見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| 編集と初期化 | 保存を確認するまで端末設定を変えないことを確認する |
| 保存と破棄 | 保存結果、権限案内、ホームへ戻る結果を確認する |
| 画面の復元 | 未保存入力と開いていた確認画面が戻ることを確認する |

## 扱う情報と結果

6件のテストで、画面に保持する状態と端末に保存する値の違いを確認します。

関連する場所: [編集の状態管理](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/LessonTimeEditorState.kt)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

