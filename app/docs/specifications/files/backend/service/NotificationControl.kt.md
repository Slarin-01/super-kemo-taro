# NotificationControl.kt：通知のミュートと解除

更新日: 2026-10-13  
対象: [NotificationControl.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationControl.kt)

## 役割

必要な許可を確認し、Androidへおやすみモードの変更を依頼します。

## 主な部分と機能

| 部分 | 担当すること |
|---|---|
| `NotificationGateway` | 通知の許可確認・状態取得・変更の窓口を定義します。 |
| `AndroidNotificationGateway` | Androidの通知機能を使って、窓口の処理を実行します。 |
| `NotificationModeService.hasAccess・currentMuted` | 許可と現在のミュート状態を確認します。 |
| `NotificationModeService.setMuted` | 指定したミュート状態への変更を依頼します。 |
| `NotificationModeService.toggle` | 現在の状態を反転し、切り替え後の状態を返します。 |

## 扱うデータと結果

- ミュート状態は`true`、解除状態は`false`で表します。
- 状態取得・切り替えで返る`null`は、許可不足を表します。
- `setMuted`の戻り値は、状態ではなく変更依頼の可否です。`true`は依頼済み、`false`は許可不足です。

## 現在の動き・設定

通知をすべて許可する状態以外は「ミュート」として扱います。ミュート時は通知を止め、解除時は通知をすべて許可する状態に変更します。解除前に使っていた細かな通知条件の復元は行いません。

## つながり・変更したいとき

通知テスト画面の状態管理と[NotificationAutomation.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/backend/service/NotificationAutomation.kt)がこの処理を使います。通知モードの扱いや許可確認を変えるときに編集します。許可設定画面を開く判断は画面側の担当です。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
