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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
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
import com.android.example.paging.pagingwithnetwork.skin.SkinServiceLocator
import com.android.example.paging.pagingwithnetwork.skin.vo.SkinPost
import kotlinx.android.synthetic.main.activity_reddit.*

/**
 * A list activity that shows reddit posts in the given sub-reddit.
 * <p>
 * The intent arguments can be modified to make it use a different repository (see MainActivity).
 */
class SkinActivity : AppCompatActivity() {
    companion object {
        val KEY_SUBREDDIT = "skin_category_id"
        val DEFAULT_SUBREDDIT = "10000001"
//        val KEY_REPOSITORY_TYPE = "repository_type"
        fun intentFor(context: Context): Intent {
            val intent = Intent(context, SkinActivity::class.java)
//            intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
            return intent
        }
    }

    private lateinit var model: SubSkinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reddit)
        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        initSearch()
        val subskin = savedInstanceState?.getString(KEY_SUBREDDIT) ?: DEFAULT_SUBREDDIT
        model.showSubskin(subskin)
    }

    private fun getViewModel(): SubSkinViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                val repoTypeParam = intent.getIntExtra(KEY_REPOSITORY_TYPE, 0)
//                val repoType = SkinPostRepository.Type.values()[repoTypeParam]
                val repo = SkinServiceLocator.instance(this@SkinActivity)
                        .getRepository()
                @Suppress("UNCHECKED_CAST")
                return SubSkinViewModel(repo) as T
            }
        })[SubSkinViewModel::class.java]
    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)
        val adapter = SkinAdapter(glide) {
            model.retry()
        }
        list.adapter = adapter
        model.posts.observe(this, Observer<PagedList<SkinPost>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SUBREDDIT, model.currentSubskin())
    }

    private fun initSearch() {
        input.setOnEditorActionListener({ _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updatedSubskinFromInput()
                true
            } else {
                false
            }
        })
        input.setOnKeyListener({ _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updatedSubskinFromInput()
                true
            } else {
                false
            }
        })
    }

    // 编辑框(input)的文字(text)去掉首尾空格(trim)后的字符串(toString)，作为参数给(let)闭包{...}执行：
    // 闭包{...}的参数(it，编辑框文本去掉首尾空格后的字符串)非空(isNotEmpty)时继续执行
    // 调用ViewModel的showSubskin方法，把编辑框字符串传进去，如果返回结果为true(表示字符串与先前查询的发生了变化)，继续执行
    // 列表控件(list)滚动到头部(scrollToPosition(0)), 列表的adapter强转(SkinAdapter)成功后把数据设为null.
    // SA: SubSkinViewModel.showSubskin(String)
    private fun updatedSubskinFromInput() {
        input.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (model.showSubskin(it)) {
                    list.scrollToPosition(0)
                    (list.adapter as? SkinAdapter)?.submitList(null)
                }
            }
        }
    }
}
