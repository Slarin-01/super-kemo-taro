# mipmap-anydpi-v26/ic_launcher_round.xml 仕様書

対象: [src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)  
確認日: 2026-10-11

## 定義

anydpi-v26修飾子のadaptive-icon。背景、前景、monochromeの3参照を持つ。

| レイヤー | 参照 |
| --- | --- |
| background | @drawable/ic_launcher_background |
| foreground | @mipmap/ic_launcher_foreground |
| monochrome | @mipmap/ic_launcher_foreground |

## 利用関係

AndroidManifest.xmlのapplication android:roundIconが@mipmap/ic_launcher_roundを指定する。対応する環境で本XMLが選択され、低いAPI向けには密度別WebPが同名のmipmapとして存在する。

通常用と丸型用のXML内容は現在同じ。前景はdrawableのベクターではなく、密度別のmipmap画像を使用する。

## 入出力

OSのアイコン描画に使うリソース。実行処理・保存データはない。

