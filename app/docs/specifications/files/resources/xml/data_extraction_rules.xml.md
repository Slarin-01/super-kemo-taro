# xml/data_extraction_rules.xml 仕様書

対象: [src/main/res/xml/data_extraction_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/xml/data_extraction_rules.xml)  
確認日: 2026-10-11

## 現内容

data-extraction-rules内に空のcloud-backup要素を定義する。include・excludeの例とdevice-transfer要素はコメントアウトされ、有効な個別対象の指定はない。

## 適用関係

現在のAndroidManifest.xmlにはandroid:dataExtractionRules="@xml/data_extraction_rules"の指定がない。このファイルから独自のクラウドバックアップ・端末移行ルールを適用する設定にはなっていない。

## 入出力

Androidリソースとして存在するXML設定。画面・保存処理・アラームを実行しない。このファイルの存在だけでSharedPreferencesのバックアップや復元を保証しない。

