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
import android.arch.paging.PageKeyedDataSource
import com.android.example.paging.pagingwithnetwork.base.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.skin.api.SkinApi
import com.android.example.paging.pagingwithnetwork.skin.vo.SkinPost
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

/**
 * A data source that uses the before/current_page keys returned in page requests.
 * <p>
 * See ItemKeyedSubskinDataSource
 */
// 按页取数据的DataSource类，从PageKeyedDataSource派生来，构造传入api封装, 关键字和Executor.
// 实现重试（retryAllFailed）和刷新（父类invalidate）方法, 保存网络和刷新对应的两个LiveData(networkState和initialLoad)，
class PageKeyedSubskinDataSource(
        private val skinApi: SkinApi,
        private val subskinName: String,
        private val retryExecutor: Executor) : PageKeyedDataSource<Int, SkinPost>() {

    // keep a function reference for the retry event
    // todo: 定义重试方法？？？
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    // 发生错误时重新加载，后台执行重试，且保证只执行一次(通过赋值给临时变量，设空，让临时变量执行)
    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    // 加载前一页，此处保持空，没有要在首页加载前面插入的页面
    override fun loadBefore(
            params: LoadParams<Int>,
            callback: LoadCallback<Int, SkinPost>) {
        // ignored, since we only ever append to our initial load
    }

    // 加载后一页数据，入参含有下一页索引值和页大小（条目数），和取到数据后的回调
    // 先向网络状态post一个LOADING状态
    // 调用api请求下个页面数据,传入关键字，页码和页面大小，返回结果失败时，设置重试为再执行本次加载，
    // 向网络状态发送error(FAIL+消息); 结果正常返回时，如果是成功的结果，从body取data（前后页码和列表），
    // 回调下一页，并发送加载完成的网络状态，否则失败的结果设置重新加载为本页，发送错误的网络状态(FAIL+含错误码的消息)
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, SkinPost>) {
        networkState.postValue(NetworkState.LOADING)
        skinApi.getSkinList(
                id = subskinName,
                after = params.key,
                limit = params.requestedLoadSize).enqueue(
                object : retrofit2.Callback<SkinApi.ListingResponse> {
                    override fun onFailure(call: Call<SkinApi.ListingResponse>, t: Throwable) {
                        retry = {
                            loadAfter(params, callback)
                        }
                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(
                            call: Call<SkinApi.ListingResponse>,
                            response: Response<SkinApi.ListingResponse>) {
                        if (response.isSuccessful) {
                            val data = response.body()?.data
                            val items = data?.items ?: emptyList()
//                            combineDataItem(data, items)
                            retry = null
                            callback.onResult(items, params.key + 1)
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = {
                                loadAfter(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )
    }

    // 首次加载页面，入参含页面大小和关键字，和取到数据后的回调
    // 从api请求首页，传入关键字和页面大小，网络状态和刷新状态发送LOADING值
    // 请求返回后，成功得到数据则网络与刷新状态发送LOADED和结果数据(callback.onResult),否则
    // 有错误，发送(FAIL+错误信息)到刷新和网络状态
    override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Int, SkinPost>) {
        val request = skinApi.getSkinList(
                id = subskinName,
                after = 0,
                limit = params.requestedLoadSize
        )
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val data = response.body()?.data
            val items = data?.items ?: emptyList()
//            combineDataItem(data, items)

            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)

            callback.onResult(items, 0, 1)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    // 把themes里的url和data.baseUrl拼接，使用map操作符
//    private fun combineDataItem(data: SkinApi.ListingResponse?, items: List<SkinPost>) {
//        items.map {
//            it.url = data?.baseResUrl + it.url
//            it.indexInResponse = data?.current_page ?: -1
//        }
//    }
}