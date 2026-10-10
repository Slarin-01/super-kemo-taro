# rules.keep 仕様書

対象: [src/main/keepRules/rules.keep](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/keepRules/rules.keep)  
確認日: 2026-10-11

## 役割と現内容

R8用の保持ルールを配置するファイル。現在は説明コメントと、WebViewのJavaScriptインターフェース向けのコメントアウトされた例だけで、有効なkeepルールはない。

## ビルドとの関係

ファイルの説明コメントではsrc/main/keepRules内のルールをAGPが集約する構成を案内している。現在のbuild.gradle.ktsはrelease.isMinifyEnabled=falseで、リリースのコード縮小を有効にしていない。

## 入出力・利用上の注意

実行時処理、画面、保存データはない。将来コード縮小を有効にする際には、実際の必要性に合わせて保持対象を定義する。現コードのproguard-rules.proは存在せず、このファイルへ推測でルールを追加していない。

