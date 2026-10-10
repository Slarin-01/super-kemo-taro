# drawable/ic_launcher_background.xml 仕様書

対象: [src/main/res/drawable/ic_launcher_background.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/drawable/ic_launcher_background.xml)  
確認日: 2026-10-11

## 定義

108dp × 108dp、viewport 108 × 108のvector drawable。#3DDC84の背景と#33FFFFFFの格子状の線をpathで定義する。

## 利用関係

mipmap-anydpi-v26/ic_launcher.xmlとic_launcher_round.xmlのbackgroundが@drawable/ic_launcher_backgroundとして参照する。アプリのランチャーアイコンの背景レイヤーで、Compose画面の背景ではない。

## 入出力

拡大縮小可能な描画リソース。実行関数・永続データ・操作コールバックはない。

