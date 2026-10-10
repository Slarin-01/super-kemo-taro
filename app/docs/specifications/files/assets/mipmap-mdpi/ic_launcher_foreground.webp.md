# res/mipmap-mdpi/ic_launcher_foreground.webp 仕様書

対象: [src/main/res/mipmap-mdpi/ic_launcher_foreground.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-mdpi/ic_launcher_foreground.webp)  
確認日: 2026-10-11

## 役割

mdpi密度向けのWebP画像。リソースIDは@mipmap/ic_launcher_foreground。同じIDの画像が他の密度フォルダーにもあり、Androidが端末構成に合わせて選択する。

## 利用関係

mipmap-anydpi-v26/ic_launcher.xmlとic_launcher_round.xmlのforegroundおよびmonochromeから参照される。@drawable/ic_launcher_foregroundとは別のリソース。

## 入出力

静的な描画素材。Compose画面内の画像表示としては使わず、画面操作・設定保存・通知処理を持たない。

