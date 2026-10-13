# SettingsMenuPage.kt：設定メニュー

更新日: 2026-10-13  
対象: [SettingsMenuPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SettingsMenuPage.kt)

## 役割

時間割の確認・編集、授業時刻の編集、通知テストを開く入口です。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `MenuActivity` | 各画面を開き、クレジットの開閉状態を復元・退避します。 |
| `MenuScreen` | 5個のボタンと、クレジットの確認画面を表示します。 |
| `MenuScreenPreview` | ライト・ダークの表示例をAndroid Studioへ渡します。 |

## 扱う情報と動き

- 状態管理から、クレジット本文と開いているかどうかを受け取ります。
- 「時間割を見る」はホーム、「時刻設定」は曜日・時限の選択、「時限カスタム」は授業時刻の編集を開きます。
- メニューを残して次の画面を開くため、時間割編集や通知テストの「戻る」でメニューへ戻れます。
- Androidが画面を作り直す場合に備え、クレジットの開閉を復元用データに残します。設定としては保存しません。

クレジットの状態と本文: [MenuState.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/MenuState.kt)  
共通の確認画面: [SharedScreenComponents.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/SharedScreenComponents.kt)

## 変更したいとき

メニューの項目、ボタン名、移動先を変えるときに編集します。
プレビューは表示確認用で、ほかの画面への移動は行いません。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
