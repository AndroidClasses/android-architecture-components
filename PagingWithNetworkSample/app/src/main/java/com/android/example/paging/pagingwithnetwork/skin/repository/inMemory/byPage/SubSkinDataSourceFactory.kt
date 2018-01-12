/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.paging.pagingwithnetwork.skin.repository.inMemory.byPage

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.android.example.paging.pagingwithnetwork.skin.api.SkinApi
import com.android.example.paging.pagingwithnetwork.skin.vo.SkinPost
import java.util.concurrent.Executor

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
// 数据源factory, 接收api的retrofit封装，关键字和后台执行网络请求的Executor，create()接口
// 给LivePagedListBuilder调用，生成DataSource。
// 构造时保存api的retrofit封装，关键字，和执行网络请求的Executor, 和一个分页数据的LiveData。
class SubSkinDataSourceFactory(
        private val skinApi: SkinApi,
        private val subskinName: String,
        private val retryExecutor: Executor) : DataSource.Factory<Int, SkinPost> {
    val sourceLiveData = MutableLiveData<PageKeyedSubskinDataSource>()
    // LivePagedListBuilder调用取得DataSource对象，这里新创建PageKeyedSubskinDataSource类型对象，传入
    // 构造时保存的api封装，关键字和Executor, 并把值post到LiveData后，返回。
    override fun create(): DataSource<Int, SkinPost> {
        val source = PageKeyedSubskinDataSource(skinApi, subskinName, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}