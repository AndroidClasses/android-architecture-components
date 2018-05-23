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

package com.android.example.paging.pagingwithnetwork.skin

import android.app.Application
import android.content.Context
import android.support.annotation.VisibleForTesting
import com.android.example.paging.pagingwithnetwork.skin.api.SkinApi
import com.android.example.paging.pagingwithnetwork.skin.repository.SkinPostRepository
import com.android.example.paging.pagingwithnetwork.skin.repository.inMemory.byPage.SkinInMemoryByPageKeyRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface SkinServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: SkinServiceLocator? = null
        fun instance(context: Context): SkinServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                            app = context.applicationContext as Application,
                            useInMemoryDb = false)
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: SkinServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(): SkinPostRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getSkinApi(): SkinApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application, val useInMemoryDb: Boolean) : SkinServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    // todo: add and change to SkinDb
//    private val db by lazy {
//        SkinDb.create(app, useInMemoryDb)
//    }

    private val api by lazy {
        SkinApi.create()
    }

    override fun getRepository(): SkinPostRepository {
        // todo: add db + networking type
        return SkinInMemoryByPageKeyRepository(
                    skinApi = getSkinApi(),
                    networkExecutor = getNetworkExecutor())

//        return when (type) {
//            SkinPostRepository.Type.IN_MEMORY_BY_PAGE -> InMemoryByPageKeyRepository(
//                    skinApi = getSkinApi(),
//                    networkExecutor = getNetworkExecutor())
//            SkinPostRepository.Type.DB -> DbSkinPostRepository(
//                    db = db,
//                    skinApi = getSkinApi(),
//                    ioExecutor = getDiskIOExecutor())
//        }
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getSkinApi(): SkinApi = api
}