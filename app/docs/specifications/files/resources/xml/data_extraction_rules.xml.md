# data_extraction_rules.xml：データ移行設定のひな形

更新日: 2026-10-13  
対象: [data_extraction_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/xml/data_extraction_rules.xml)

## 役割

バックアップや端末間のデータ移行の対象を指定するためのひな形です。現在はManifestから指定されていません。

## 変更したいとき

このファイルで移行対象を制御する場合に、内容とManifestの指定を見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| cloud-backup | バックアップ対象を指定するためのひな形 |

## 扱う情報と結果

現在はManifestからこのファイルを指定していません。コメント内の記述例も実行されないため、個別のバックアップ設定として使用していません。

関連する場所: [現在の登録内容](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/AndroidManifest.xml)

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

