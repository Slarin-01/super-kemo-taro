/*
 * フロントエンド・状態管理: 画面ごとのViewModelを作る共通の仕組み。
 * 状態の再利用はViewModelProviderが担当する。このFactoryは新規作成が必要なときだけ使われる。
 */
package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// 1. 画面状態の生成と再利用
/** 必要なサービスを受け取るViewModelの作り方を、画面からViewModelProviderへ渡す。 */
class ScreenViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val model = creator()
        // 間違った種類のViewModelを登録した場合、ここで検出して原因を明確にする。
        require(modelClass.isInstance(model))
        return modelClass.cast(model)!!
    }
}

