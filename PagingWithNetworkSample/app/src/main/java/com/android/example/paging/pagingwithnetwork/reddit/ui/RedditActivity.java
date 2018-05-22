///*
// * Copyright (C) 2017 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.android.example.paging.pagingwithnetwork.reddit.ui
//
//import android.arch.lifecycle.ViewModelProvider;
//import android.arch.lifecycle.ViewModelProviders;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//
//import com.android.example.paging.pagingwithnetwork.R;
//import com.android.example.paging.pagingwithnetwork.reddit.ServiceLocator;
//import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.RequestManager;
//
//
///**
// * A list activity that shows reddit posts in the given sub-reddit.
// * <p>
// * The intent arguments can be modified to make it use a different repository (see MainActivity).
// */
//public class RedditActivity extends AppCompatActivity {
//    public static final String KEY_SUBREDDIT = "subreddit";
//    public static final String DEFAULT_SUBREDDIT = "androiddev";
//    public static final String KEY_REPOSITORY_TYPE = "repository_type";
//    public static Intent intentFor(Context context, RedditPostRepository.Type type) {
//        Intent intent = new Intent(context, RedditActivity.class);
//        intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal());
//        return intent;
//    }
//
//    private SubRedditViewModel model;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_reddit);
//        model = getViewModel();
//        initAdapter();
//        initSwipeToRefresh();
//        initSearch();
//        String subreddit = null == savedInstanceState ? DEFAULT_SUBREDDIT : savedInstanceState.getString(KEY_SUBREDDIT);
//        model.showSubreddit(subreddit);
//    }
//
//    class ShowsViewModelFactory implements ViewModelProvider.Factory {
//        @Override
//        public SubRedditViewModel create(Class modelClass) {
//            int repoTypeParam = getIntent().getIntExtra(KEY_REPOSITORY_TYPE, 0);
//            RedditPostRepository.Type repoType = RedditPostRepository.Type.values()[repoTypeParam];
//            RedditPostRepository repo = new ServiceLocator(RedditActivity.this).getRepository(repoType);
//            return new SubRedditViewModel(repo);
//        }
//    }
//
//    private SubRedditViewModel getViewModel() {
//        return ViewModelProviders.of(this, new ShowsViewModelFactory()).get(SubRedditViewModel.class);
//    }
//
//    private void initAdapter() {
//        RequestManager glide = Glide.with(this);
//        PostsAdapter adapter = new PostsAdapter(glide, new );
//    }
//}
//
//class RedditActivity : AppCompatActivity() {
//    companion object {
//        val KEY_SUBREDDIT = "subreddit"
//        val DEFAULT_SUBREDDIT = "androiddev"
//        val KEY_REPOSITORY_TYPE = "repository_type"
//        fun intentFor(context: Context, type: RedditPostRepository.Type): Intent {
//            val intent = Intent(context, RedditActivity_::class.java)
//            intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
//            return intent
//        }
//    }
//
//    private lateinit var model: SubRedditViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_reddit)
//        model = getViewModel()
//        initAdapter()
//        initSwipeToRefresh()
//        initSearch()
//        val subreddit = savedInstanceState?.getString(KEY_SUBREDDIT) ?: DEFAULT_SUBREDDIT
//        model.showSubreddit(subreddit)
//    }
//
//    private fun getViewModel(): SubRedditViewModel {
//        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
//            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                val repoTypeParam = intent.getIntExtra(KEY_REPOSITORY_TYPE, 0)
//                val repoType = RedditPostRepository.Type.values()[repoTypeParam]
//                val repo = ServiceLocator.instance(this@RedditActivity_)
//                        .getRepository(repoType)
//                @Suppress("UNCHECKED_CAST")
//                return SubRedditViewModel(repo) as T
//            }
//        })[SubRedditViewModel::class.java]
//    }
//
//    private fun initAdapter() {
//        val glide = Glide.with(this)
//        val adapter = PostsAdapter(glide) {
//            model.retry()
//        }
//        list.adapter = adapter
//        model.posts.observe(this, Observer<PagedList<RedditPost>> {
//            adapter.submitList(it)
//        })
//        model.networkState.observe(this, Observer {
//            adapter.setNetworkState(it)
//        })
//    }
//
//    private fun initSwipeToRefresh() {
//        model.refreshState.observe(this, Observer {
//            swipe_refresh.isRefreshing = it == NetworkState.LOADING
//        })
//        swipe_refresh.setOnRefreshListener {
//            model.refresh()
//        }
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString(KEY_SUBREDDIT, model.currentSubreddit())
//    }
//
//    private fun initSearch() {
//        input.setOnEditorActionListener({ _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_GO) {
//                updatedSubredditFromInput()
//                true
//            } else {
//                false
//            }
//        })
//        input.setOnKeyListener({ _, keyCode, event ->
//            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                updatedSubredditFromInput()
//                true
//            } else {
//                false
//            }
//        })
//    }
//
//    private fun updatedSubredditFromInput() {
//        input.text.trim().toString().let {
//            if (it.isNotEmpty()) {
//                if (model.showSubreddit(it)) {
//                    list.scrollToPosition(0)
//                    (list.adapter as? PostsAdapter)?.submitList(null)
//                }
//            }
//        }
//    }
//}
