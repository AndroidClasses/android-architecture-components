package com.android.example.paging.pagingwithnetwork.reddit.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.android.example.paging.pagingwithnetwork.base.repository.BasePostRepository
import com.android.example.paging.pagingwithnetwork.reddit.ServiceLocator

/**
 * Created by yangfeng on 2018/1/13.
 */
class RedditViewModelFactory(private val context: Context, private val repoTypeParam: Int):
        ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val repoType = BasePostRepository.Type.values()[repoTypeParam]
        val repo = ServiceLocator.instance(context)
                .getRepository(repoType)
        @Suppress("UNCHECKED_CAST")
        return SubRedditViewModel(repo) as T
    }
}