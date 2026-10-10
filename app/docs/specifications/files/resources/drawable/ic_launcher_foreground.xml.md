# drawable/ic_launcher_foreground.xml 仕様書

対象: [src/main/res/drawable/ic_launcher_foreground.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/drawable/ic_launcher_foreground.xml)  
確認日: 2026-10-11

## 定義

108dp × 108dp、viewport 108 × 108のvector drawable。白色の前景pathと、半透明黒から透明への線形グラデーションpathを定義する。

## 利用関係

現アダプティブアイコンはforeground・monochromeとも@**mipmap**/ic_launcher_foregroundを参照しているため、@**drawable**/ic_launcher_foregroundの本ファイルはその参照先ではない。両者は同名でもリソース種別が異なる。

現在のKotlin・Manifest・リソースで本drawableへの参照はない。画面・保存・通知の処理には関与しない。

