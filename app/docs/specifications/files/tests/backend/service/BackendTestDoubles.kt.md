# BackendTestDoubles.kt：テスト用の代役

更新日: 2026-10-13  
対象: [BackendTestDoubles.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/test/java/com/example/super_kemo_taro3/backend/service/BackendTestDoubles.kt)

## 役割

端末の保存・通知・予約を実行する代わりに、何を依頼されたか記録します。

## 主な動き

- ほかのテストが、保存値や予約の内容を確認するために使います。

## 変更したいとき

対応する機能のルールを変えたら、確認する内容も合わせて見直します。

## 主な機能

| 部分 | 担当すること |
| --- | --- |
| MemorySettingsRepository | 設定値をメモリーに保存する代役 |
| RecordingAlarmScheduler | 登録・取消・次週予約の依頼を記録する代役 |
| RecordingNotificationGateway | 通知変更の依頼を記録する代役 |

## 扱う情報と結果

テストから許可の有無や現在の状態を指定します。実端末を操作せず、依頼の履歴をほかのテストへ返します。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)

