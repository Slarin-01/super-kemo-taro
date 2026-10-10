# ExampleInstrumentedTest.kt 仕様書

対象: [src/androidTest/java/com/example/super_kemo_taro3/ExampleInstrumentedTest.kt](C:/Users/REOTO/AndroidStudioProjects/superkemotaro3/app/src/androidTest/java/com/example/super_kemo_taro3/ExampleInstrumentedTest.kt)  
確認日: 2026-10-11

## 役割

AndroidJUnit4でAndroid端末またはエミュレーター上のアプリContextを確認する。class ExampleInstrumentedTestを@RunWith(AndroidJUnit4::class)で実行する。

## useAppContext

InstrumentationRegistry.getInstrumentation().targetContextから対象アプリのContextを取得し、packageNameがcom.example.super_kemo_taro3であることをassertEqualsで検証する。

## 実行条件

build.gradle.ktsのtestInstrumentationRunnerはandroidx.test.runner.AndroidJUnitRunner。実行には接続済み端末またはエミュレーターが必要で、 :app:connectedDebugAndroidTest を用いる。テストAPKのコンパイル成功だけでは、このテストを端末上で実行したことにならない。

## 検証範囲

画面描画、ボタン、権限、ミュートの実動作、アラームの発火は検証しない。仕様書作成時点で端末上の実行結果は確認していない。

