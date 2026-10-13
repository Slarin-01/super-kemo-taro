# ScreenStateFactory.kt：画面の状態管理を用意する部分

更新日: 2026-10-13  
対象: [ScreenStateFactory.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/viewmodel/ScreenStateFactory.kt)

## 役割

各画面で必要な状態管理（ViewModel）を作るための共通の仕組みです。

## ファイル内の主な部分

| コードの部分 | 担当 |
|---|---|
| `ScreenViewModelFactory` | 画面から受け取った、状態管理の作り方を持ちます。 |
| `create` | 必要な状態管理を作り、要求された種類か確認して返します。 |

## 扱う情報と動き

- 入力は、サービスなどを渡して状態管理を作る処理です。
- 結果は、画面から要求された種類のViewModelです。種類が違えば、その場でエラーになります。
- 画面回転で状態管理を再利用するのはAndroidの `ViewModelProvider` です。このファイルは新規作成が必要なときだけ呼ばれます。
- プロセス終了後の復元は、このファイルだけではできません。各画面の復元データと状態管理が担当します。

利用例: [HomePage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/HomePage.kt)、[LessonTimeEditorPage.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/main/java/com/example/super_kemo_taro3/frontend/screens/LessonTimeEditorPage.kt)

## 変更したいとき

新しい画面の状態管理を作る仕組みや、種類の確認方法を変えるときに編集します。
普段は各画面側で、必要なサービスを渡して使います。

[仕様書の一覧へ](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/docs/specifications/README.md)
