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

package com.android.example.paging.pagingwithnetwork.skin.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import com.android.example.paging.pagingwithnetwork.skin.repository.SkinPostRepository

/**
 * A RecyclerView ViewHolder that displays a single skin post.
 *
 * 思考：在这个ViewModel里只看到关键字变化时，从仓库加载一页(30个）数据，
 * 没有翻页逻辑，那么显示加载首页30个数据以后的页面，是什么样的过程（ui转菊花和数据刷新）
 *
 */
class SubSkinViewModel(private val repository: SkinPostRepository) : ViewModel() {
    // 关键字字符串的LiveData,保存当前值，在showSubskin方法传入新值时，更新新值后触发map操作符，从数据仓库
    // 读一页数据保存到repoResult里。
    private val subskinName = MutableLiveData<String>()
    // 关键字字符串被设置给(subskinName)时map操作符把MutableLiveData<String>
    // 数据(subskinName)映射为Listing<SkinPost>数据(repoResult), 映射过程是从仓库(repository)取该关键字的一页数据(30个)
    // 这个数据保存有读取数据所有可能的结果：成功获得数据页（Paging库的PagedList<T>），网络状态，刷新状态，以及
    // 刷新和重试两个方法。
    // SA: Listing<T>
    // SA: SkinPostRepository.postsOfSubSkin
    private val repoResult = map(subskinName, {
        repository.postsOfSubSkin(it, 30)
    })
    // 以关键字获取数据结果Listing<T>数据(repoResult)后，操作符switchMap把取到的数据分页列表
    // PagedList<T>成员映射到变量posts(LiveData)供外部进行view观察
    // map和switchMap的区别：前者结果是一个普通数据类型，后者结果则是一个LiveData
    // SA: Listing.pagedList
    val posts = switchMap(repoResult, { it.pagedList })!!
    // 与posts类似，把网络状态的LiveData映射出来供外部View观察
    val networkState = switchMap(repoResult, { it.networkState })!!
    // 与posts类似，把刷新状态的LiveData映射出来供外部View观察
    val refreshState = switchMap(repoResult, { it.refreshState })!!

    // 刷新操作，直接调用Listing的刷新方法refresh()
    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    // 显示关键字(subskin)字符串的内容，若关键字没发生变化，直接返回false，否则更改包装关键字的LiveData的值后返回true.
    // SA: subskinName关键字LiveData(MutableLiveData<String>)设置为新的字符串后的，触发获取数据流程，map操作符把MutableLiveData<String>
    // 数据(subskinName)映射为Listing<SkinPost>数据(repoResult), 映射过程是从仓库(repository)取该关键字的一页数据(30个)
    fun showSubskin(subskin: String): Boolean {
        if (subskinName.value == subskin) {
            return false
        }
        subskinName.value = subskin
        return true
    }

    // 重试操作，直接调用Listing的重试方法
    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    // 当前关键字封装subskinName里取值
    fun currentSubskin(): String? = subskinName.value
}