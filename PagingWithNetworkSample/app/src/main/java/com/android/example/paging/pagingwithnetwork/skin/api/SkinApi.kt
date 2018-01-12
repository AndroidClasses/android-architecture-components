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

package com.android.example.paging.pagingwithnetwork.skin.api

import android.util.Log
import com.android.example.paging.pagingwithnetwork.skin.vo.SkinPost
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API communication setup
 */
interface SkinApi {
    // for after/before param, either get from SkinDataResponse.after/before,
    // or pass SkinNewsDataResponse.name (though this is technically incorrect)
    @GET("getallfeaturetheme")
    fun getTopAfter(
            @Query("page") after: Int,
            @Query("pcount") limit: Int): Call<ListingResponse>

    class ListingResponse(val data: ListingData)

    class ListingData(
            val children: List<SkinChildrenResponse>,
            val after: Int?,
            val before: Int?
    )

    data class SkinChildrenResponse(val data: SkinPost)

    companion object {
        private const val BASE_URL = "http://www.typany.com/api/"
        fun create(): SkinApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): SkinApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SkinApi::class.java)
        }
    }
}