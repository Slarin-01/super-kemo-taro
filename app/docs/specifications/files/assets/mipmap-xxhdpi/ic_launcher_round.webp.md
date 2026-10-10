# res/mipmap-xxhdpi/ic_launcher_round.webp 仕様書

対象: [src/main/res/mipmap-xxhdpi/ic_launcher_round.webp](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp)  
確認日: 2026-10-11

## 役割

xxhdpi密度向けのWebP画像。リソースIDは@mipmap/ic_launcher_round。同じIDの画像が他の密度フォルダーにもあり、Androidが端末構成に合わせて選択する。

## 利用関係

AndroidManifest.xmlのapplication android:roundIconで指定される。anydpi-v26の同名XMLがアダプティブアイコンを提供する。

## 入出力

静的な描画素材。Compose画面内の画像表示としては使わず、画面操作・設定保存・通知処理を持たない。

