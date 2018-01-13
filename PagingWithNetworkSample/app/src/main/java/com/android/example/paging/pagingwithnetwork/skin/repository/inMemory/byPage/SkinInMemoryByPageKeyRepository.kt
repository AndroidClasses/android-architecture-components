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

import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import com.android.example.paging.pagingwithnetwork.base.repository.Listing
import com.android.example.paging.pagingwithnetwork.skin.api.SkinApi
import com.android.example.paging.pagingwithnetwork.skin.repository.SkinPostRepository
import com.android.example.paging.pagingwithnetwork.skin.vo.SkinPost
import java.util.concurrent.Executor

/**
 * Repository implementation that returns a Listing that loads data directly from network by using
 * the previous / next page keys returned in the query.
 */
// 直接从网络取上一页/下一页数据，网络通过Retrofit封装的api，执行在后台Executor，实现接口postsOfSubskin()
class SkinInMemoryByPageKeyRepository(private val skinApi: SkinApi,
                                      private val networkExecutor: Executor) : SkinPostRepository {
    // 通过LivePagedListBuilder来构建分页请求数据的LiveData, 传进去一页条目数和数据源的factory，返回的
    // Listing结构保存这个LiveData还有数据源factory里的网络和刷新状态的LiveData(switchMap映射)，并把重试和
    // 刷新的请求转给PageKeyedSubskinDataSource里的方法retryAllFailed/invalidate()
    // SA: SubSkinDataSourceFactory, 数据源factory, 接收api的retrofit封装，关键字和后台执行网络请求的Executor，
    // create()接口给LivePagedListBuilder调用，生成DataSource。
    @MainThread
    override fun postsOfSubskin(subskinName: String, pageSize: Int): Listing<SkinPost> {
        val sourceFactory = SubSkinDataSourceFactory(skinApi, subskinName, networkExecutor)

        val livePagedList = LivePagedListBuilder(sourceFactory, pageSize)
                // provide custom executor for network requests, otherwise it will default to
                // Arch Components' IO pool which is also used for disk access
                .setBackgroundThreadExecutor(networkExecutor)
                .build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData, {
                    it.networkState
                }),
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }
}

