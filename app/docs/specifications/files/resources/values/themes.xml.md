# themes.xml：画面起動時の基本テーマ

更新日: 2026-10-13  
対象: [themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/themes.xml)

## 役割

アプリの画面を開くときの基本的な表示設定です。タイトルバーを表示しないテーマを指定します。

## 変更したいとき

画面起動時の基本テーマを変えるときに編集します。Compose内の配色は共通画面部品で設定します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| Base.Theme.Superkemotaro3 | 明暗に対応し、標準タイトルバーを表示しない基本テーマ |
| Theme.Superkemotaro3 | Manifestが指定するアプリ用テーマ |

## 扱う情報と結果

画面起動時の基本表示に使います。画面内のCompose部品の配色は、共通画面部品が担当します。

関連する場所: [テーマの登録](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml) / [画面内の配色](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

