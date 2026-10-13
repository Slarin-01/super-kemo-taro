# ApplicationStartupTest.kt：端末上での起動確認テスト

更新日: 2026-10-13  
対象: [ApplicationStartupTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/androidTest/java/com/example/super_kemo_taro3/ApplicationStartupTest.kt)

## 役割

アプリ起動の準備と、ホーム画面の起動先が正しいか確認するテストです。

## 主な動き

- 確認項目は2件です。前回はコンパイルの確認までで、端末では実行していません。

## 変更したいとき

対応する機能のルールを変えたら、確認する内容も合わせて見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| アプリの準備 | 起動時にBellApplicationが使われることを確認する |
| ホームの起動 | 起動アイコンの設定がHomeActivityにつながることを確認する |

## 扱う情報と結果

Android端末・エミュレーターで実行する2件のテストです。単体テストとは別で、今回もコードのコンパイル確認と端末上の実行を区別します。

関連する場所: [起動の登録](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

