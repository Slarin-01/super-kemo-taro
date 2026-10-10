# values-night/colors.xml 仕様書

対象: [src/main/res/values-night/colors.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values-night/colors.xml)  
確認日: 2026-10-11

## 定義

color button_strokeを#FFCCCCCCとして定義する。通常定義#FF333333に対するnight修飾子の上書き。

## 利用関係

black・whiteはここでは定義せず、通常リソースを使用できる。現在のCompose画面の配色はAppUi.ktのKemoThemeと各UIのColor定義により決まり、button_strokeは直接使用していない。

## 入出力

実行処理・保存データはない。夜間設定に対応するAndroid色リソースを提供する。

