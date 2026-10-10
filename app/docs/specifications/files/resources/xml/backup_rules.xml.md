# xml/backup_rules.xml 仕様書

対象: [src/main/res/xml/backup_rules.xml](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/res/xml/backup_rules.xml)  
確認日: 2026-10-11

## 現内容

full-backup-content要素があり、有効なinclude・exclude要素はない。SharedPreferencesのincludeとdevice.xmlのexcludeはコメントアウトされた例。

## 適用関係

現在のAndroidManifest.xmlにはandroid:fullBackupContent="@xml/backup_rules"の指定がない。このファイルで独自のバックアップ対象を制御する設定にはなっていない。applicationのallowBackupはtrue。

## 境界条件

ファイル内のコメントはテンプレートの説明であり、それだけで適用OSや実際のバックアップ動作を確定しない。設定キーや端末依存データのバックアップ可否を保証するファイルとして扱わない。実行処理はない。

