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

package com.android.example.paging.pagingwithnetwork.reddit.repository

import com.android.example.paging.pagingwithnetwork.base.repository.Listing
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost

/**
 * Common interface shared by the different repository implementations.
 * Note: this only exists for sample purposes - typically an app would implement a repo once, either
 * network+db, or network-only
 */
// 数据仓库接口，查询某个关键字的posts页面，每页pageSize个数据。ViewModel类在关键字改变时调用。
// 有3种类型的数据仓库实现，取缓存在内存里的网络数据(分页或者按条目标识取)或者同时取数据库+网络数据
interface RedditPostRepository {
    fun postsOfSubreddit(subReddit: String, pageSize: Int): Listing<RedditPost>

    enum class Type {
        IN_MEMORY_BY_ITEM,
        IN_MEMORY_BY_PAGE,
        DB
    }
}