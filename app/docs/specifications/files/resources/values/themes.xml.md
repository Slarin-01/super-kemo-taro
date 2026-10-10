# values/themes.xml 仕様書

対象: [src/main/res/values/themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values/themes.xml)  
確認日: 2026-10-11

## 定義

| スタイル | 親 |
| --- | --- |
| Base.Theme.Superkemotaro3 | Theme.Material3.DayNight.NoActionBar |
| Theme.Superkemotaro3 | Base.Theme.Superkemotaro3 |

## 利用関係

AndroidManifest.xmlのapplication android:themeでTheme.Superkemotaro3を指定する。全Activityに共通のDayNight・ActionBarなしの基本テーマを与える。基底のMaterialテーマにはbuild.gradle.ktsのlibs.materialが関係する。

Compose画面内のMaterialThemeは別途AppUi.ktのKemoThemeが設定する。XMLテーマの存在はXMLで画面レイアウトを構築していることを意味しない。旧SquareTimeButtonスタイルは現ファイルにない。

## 上書き

Base.Theme.Superkemotaro3はvalues-night/themes.xmlにも定義される。本ファイルに有効な個別色itemの設定はなく、例示itemはコメントのみ。保存データや実行関数はない。

