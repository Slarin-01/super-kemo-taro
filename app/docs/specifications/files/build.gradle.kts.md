# build.gradle.kts：アプリを組み立てる設定

更新日: 2026-10-13  
対象: [build.gradle.kts](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/build.gradle.kts)

## 役割

対応するAndroidの条件、アプリを識別するID、バージョンを設定します。現在はAndroid 7.0以上が対象です。Composeの画面・プレビューとテストに必要なライブラリも指定します。

## 主な動き

- Composeの画面とプレビューを使えるようにします。
- 確認用のDebug版と、配布用のRelease版を作る設定があります。

## 変更したいとき

対応するAndroidの条件、ライブラリ、ビルド設定を変えるときに編集します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| android | アプリID、対応するAndroid、バージョンを指定する |
| buildTypes / compileOptions | 配布版の作り方とJavaコードの条件を指定する |
| dependencies | 画面・プレビュー・テストに必要なライブラリを指定する |

## 扱う情報と結果

この設定を使って、確認用のDebug版と配布用のRelease版を作ります。現在はAndroid 7.0以上に対応し、Composeで画面を作ります。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

