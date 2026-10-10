# values-night/themes.xml 仕様書

対象: [src/main/res/values-night/themes.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/values-night/themes.xml)  
確認日: 2026-10-11

## 定義

Base.Theme.Superkemotaro3をTheme.Material3.DayNight.NoActionBarの子として定義する。night修飾子で通常の基底スタイルを上書きする。

## 利用関係

Theme.Superkemotaro3自体の定義は通常のvalues/themes.xmlにあり、その親であるBase.Theme.Superkemotaro3をここで提供する。MainActivityとSuperKemoTaroAppがAppCompatDelegateで明暗モードを適用する。

個別の色itemは未設定。Compose内部の配色はAppUi.ktのKemoThemeで決まる。実行処理・保存データはない。

