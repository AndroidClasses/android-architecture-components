package com.android.example.paging.pagingwithnetwork.skin.api

import okhttp3.HttpUrl.Builder
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Created by yangfeng on 2018/1/14.
 */
// todo: add parameter
// proto=0&page=0&pcount=2&id=10000001&eid=3d1a633e36f70a27_868029020935367&pla=android&pro=ime&build=1&typ=1&rhe=1080&rwi=1920&clan=zh&bra=Xiaomi&ver=196370&mod=RedmiNote3&sys=21&lan=zh&cou=CN&ofr=gime&fr=gime
internal class CommonInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        // 添加新的参数
        val authorizedUrlBuilder = oldRequest.url()
                .newBuilder()
                .scheme(oldRequest.url().scheme())
                .host(oldRequest.url().host())

        addQueryParameter(authorizedUrlBuilder)
        //                .addQueryParameter(MarvelService.PARAM_API_KEY, mApiKey)
        //                .addQueryParameter(MarvelService.PARAM_TIMESTAMP, MarvelApiUtils.getUnixTimeStamp())
        //                .addQueryParameter(MarvelService.PARAM_HASH, marvelHash);

        // 新的请求
        val newRequest = oldRequest.newBuilder()
                .method(oldRequest.method(), oldRequest.body())
                .url(authorizedUrlBuilder.build())
                .build()
        return chain.proceed(newRequest)
    }

    // todo: 固定参数的值根据运行系统获取
    private fun addQueryParameter(builder: Builder) {
        builder.addQueryParameter("proto", "0")
        //        builder.addQueryParameter("page", "0");
        //        builder.addQueryParameter("pcount", "2");
        //        builder.addQueryParameter("id", "10000001");
        builder.addQueryParameter("eid", "3d1a633e36f70a27_868029020935367")
        builder.addQueryParameter("pla", "android")
        builder.addQueryParameter("pro", "ime")
        builder.addQueryParameter("build", "1")
        builder.addQueryParameter("typ", "1")
        builder.addQueryParameter("rhe", "1080")
        builder.addQueryParameter("rwi", "1920")
        builder.addQueryParameter("clan", "zh")
        builder.addQueryParameter("bra", "Xiaomi")
        builder.addQueryParameter("ver", "196370")
        builder.addQueryParameter("mod", "RedmiNote3")
        builder.addQueryParameter("sys", "21")
        builder.addQueryParameter("lan", "zh")
        builder.addQueryParameter("cou", "CN")
        builder.addQueryParameter("ofr", "gime")
        builder.addQueryParameter("fr", "gime")
    }
}