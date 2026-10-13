# MenuState.kt：クレジットの表示状態

更新日: 2026-10-13  
対象: [MenuState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/MenuState.kt)

## 役割

クレジットに表示する文章と、開いているかどうかを管理します。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `MenuViewModel`・`showCredits` | 復元した開閉状態を受け取り、画面が使う状態を持ちます。 |
| `openCredits`・`closeCredits` | クレジットの開閉を切り替えます。 |
| `creditMessage` | アプリ名、メンバー、使用ツール、対応OSなどの本文を用意します。 |

## 扱う情報と動き

- 作成時に、復元した開閉状態を受け取れます。指定がなければ閉じた状態で始めます。
- 開閉が変わると、画面もその状態に合わせて表示します。
- 本文は固定の文章です。設定保存、通知制御、画面への移動は行いません。
- プロセス終了後に備えた開閉状態の退避と復元は、画面側が担当します。

表示・復元・画面への移動: [SettingsMenuPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SettingsMenuPage.kt)

## 変更したいとき

クレジット本文と、その開閉の扱いを変えるときに編集します。
対応OSなどを変えた場合は、本文も実態に合わせて更新します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
