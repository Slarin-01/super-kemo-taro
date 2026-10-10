# values/colors.xml 仕様書

対象: [src/main/res/values/colors.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/colors.xml)  
確認日: 2026-10-11

## 定義

| 色名 | 値 |
| --- | --- |
| black | #FF000000 |
| white | #FFFFFFFF |
| button_stroke | #FF333333 |

## 利用関係

Androidリソースとして色を提供する。現在のCompose画面はMaterialThemeやColor.Green・LightGray・Blackをコードで使用し、この色定義を直接参照していない。旧XMLレイアウトのボタン枠線用のbutton_strokeが残っている。

button_strokeはvalues-night/colors.xmlに夜間用の上書き定義がある。このファイルには処理・保存データはない。

