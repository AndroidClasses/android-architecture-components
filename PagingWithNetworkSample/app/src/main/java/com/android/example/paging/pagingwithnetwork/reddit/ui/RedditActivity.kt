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

package com.android.example.paging.pagingwithnetwork.reddit.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.android.example.paging.pagingwithnetwork.GlideApp
import com.android.example.paging.pagingwithnetwork.R
import com.android.example.paging.pagingwithnetwork.base.repository.NetworkState
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost
import kotlinx.android.synthetic.main.activity_reddit.*

/**
 * A list activity that shows reddit posts in the given sub-reddit.
 * <p>
 * The intent arguments can be modified to make it use a different repository (see MainActivity).
 */
class RedditActivity : AppCompatActivity() {
    // 静态常量和函数
    companion object {
        // 根据不同的类型参数，创建不同的Intent，把参数会给activity
        const val KEY_SUBREDDIT = "subreddit"
        const val DEFAULT_SUBREDDIT = "androiddev"
        const val KEY_REPOSITORY_TYPE = "repository_type"
        fun intentFor(context: Context, type: RedditPostRepository.Type): Intent {
            val intent = Intent(context, RedditActivity::class.java)
            intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
            return intent
        }
    }

    // ViewModel对象，todo: lateinit关键字表示对象变量延后赋值？？？
    private lateinit var model: SubRedditViewModel

    // 创建activity对象时，设置content view, 创建view model, 初始化adapter，下拉刷新和搜索框，
    // 读取之前保存的搜索关键字或者用默认初始值，然后作为参数调用view model的方法显示关键字的数据
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reddit)
        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        initSearch()
        val subreddit = savedInstanceState?.getString(KEY_SUBREDDIT) ?: DEFAULT_SUBREDDIT
        model.showSubreddit(subreddit)
    }

    // 创建view model, 通过实现ViewModelProvide.Factory来控制view model构造时把想要的数据来源（仓库类型）
    // 对应参数和activity的Context传递给对应于它的Factory类(RedditViewModelFactory, 并由创建view model
    // 同时把参数传过去
    private fun getViewModel(): SubRedditViewModel {
        val repoTypeParam = intent.getIntExtra(KEY_REPOSITORY_TYPE, 0)
        return ViewModelProviders.of(this,
                RedditViewModelFactory(this, repoTypeParam))[SubRedditViewModel::class.java]
    }

    // 初始化图片加载库Glide， 创建页面视图的Adapter（完成后），运行闭包调view model重新取数据(retry)
    // 观察view model里的post列表的LiveData，当数据变化时把分页列表PagedList设置给adapter
    // 观察view model里网络状态(networkState)的LiveData, 数据变化时，设置到adapter里
    private fun initAdapter() {
        val glide = GlideApp.with(this)
        val adapter = PostsAdapter(glide) {
            model.retry()
        }
        list.adapter = adapter
        model.posts.observe(this, Observer<PagedList<RedditPost>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    // 初始化下拉刷新，观察刷新状态LiveData，刷新控件刷新，调view model刷新(refresh)一下
    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }
    }

    // 保留既出前的搜索关键词，重新创建时重新读入
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SUBREDDIT, model.currentSubreddit())
    }

    // 初始化搜索框，监听事件，更新关键词
    private fun initSearch() {
        input.setOnEditorActionListener({ _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updatedSubredditFromInput()
                true
            } else {
                false
            }
        })
        input.setOnKeyListener({ _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updatedSubredditFromInput()
                true
            } else {
                false
            }
        })
    }

    // 编辑框(input)的文字(text)去掉首尾空格(trim)后的字符串(toString)，作为参数给(let)闭包{...}执行：
    // 闭包{...}的参数(it，编辑框文本去掉首尾空格后的字符串)非空(isNotEmpty)时继续执行
    // 调用ViewModel的showSubreddit方法，把编辑框字符串传进去，如果返回结果为true(表示字符串与先前查询的发生了变化)，继续执行
    // 列表控件(list)滚动到头部(scrollToPosition(0)), 列表的adapter强转(PostsAdapter)成功后把数据设为null.
    // SA: SubRedditViewModel.showSubreddit(String)
    private fun updatedSubredditFromInput() {
        input.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (model.showSubreddit(it)) {
                    list.scrollToPosition(0)
                    (list.adapter as? PostsAdapter)?.submitList(null)
                }
            }
        }
    }
}
