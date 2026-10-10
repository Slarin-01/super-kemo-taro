# values/strings.xml 仕様書

対象: [src/main/res/values/strings.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/strings.xml)  
確認日: 2026-10-11

## 定義

resources内にstring app_nameを1個定義し、値は「言十ベル」。

## 利用関係

AndroidManifest.xmlのapplication android:labelが@string/app_nameを参照する。アプリのラベルとして使用される。Composeのボタン・ダイアログなどの文言は各Kotlinファイルに直接記述されており、このファイルには集約されていない。

## 保存・動作

実行処理と永続データはない。MainActivity3.creditMessage内のアプリ名は別の固定文字列で、自動連動しない。

