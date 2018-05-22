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

package com.android.example.paging.pagingwithnetwork.reddit.ui;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;
import android.text.TextUtils;

import com.android.example.paging.pagingwithnetwork.reddit.repository.Listing;
import com.android.example.paging.pagingwithnetwork.reddit.repository.NetworkState;
import com.android.example.paging.pagingwithnetwork.reddit.repository.RedditPostRepository;
import com.android.example.paging.pagingwithnetwork.reddit.vo.RedditPost;

import static android.arch.lifecycle.Transformations.map;
import static android.arch.lifecycle.Transformations.switchMap;

/**
 * A RecyclerView ViewHolder that displays a single reddit post.
 * Kotlin to Java converted
 */
public class SubRedditViewModel extends ViewModel {
    private final RedditPostRepository repository;

    public final MutableLiveData<String> subredditName = new MutableLiveData<>();
    public final LiveData<Listing<RedditPost>> repoResult = map(subredditName, new Function<String, Listing<RedditPost>>() {
        @Override
        public Listing<RedditPost> apply(String it) {
            return repository.postsOfSubreddit(it, 30);
        }
    });

    public final LiveData<PagedList<RedditPost>> posts = switchMap(repoResult, new Function<Listing<RedditPost>, LiveData<PagedList<RedditPost>>>() {
        @Override
        public LiveData<PagedList<RedditPost>> apply(Listing<RedditPost> input) {
            return input.getPagedList();
        }
    });

    public final LiveData<NetworkState> networkState = switchMap(repoResult, new Function<Listing<RedditPost>, LiveData<NetworkState>>() {
        @Override
        public LiveData apply(Listing<RedditPost> input) {
            return input.getNetworkState();
        }
    });

    public final LiveData<NetworkState> refreshState = switchMap(repoResult, new Function<Listing<RedditPost>, LiveData<NetworkState>>() {
        @Override
        public LiveData<NetworkState> apply(Listing<RedditPost> input) {
            return input.getRefreshState();
        }
    });

    public SubRedditViewModel(RedditPostRepository repository) {
        this.repository = repository;
    }

    public void refresh() {
        if (null != repoResult.getValue()) {
            repoResult.getValue().getRefresh().invoke();
        }
    }

    public boolean showSubreddit(String subreddit) {
        if (TextUtils.equals(subreddit, subredditName.getValue())) {
            return false;
        }
        subredditName.setValue(subreddit);
        return true;
    }

    public void retry() {
        if (null != repoResult) {
            Listing<RedditPost> listing = repoResult.getValue();
            if (null != listing) {
                listing.getRetry().invoke();
            }
        }
    }

    public String currentSubreddit() {
        return subredditName.getValue();
    }
}

//class SubRedditViewModel(private val repository: RedditPostRepository) : ViewModel() {
//    private val subredditName = MutableLiveData<String>()
//    private val repoResult = map(subredditName, {
//        repository.postsOfSubreddit(it, 30)
//    })
//    val posts = switchMap(repoResult, { it.pagedList })!!
//    val networkState = switchMap(repoResult, { it.networkState })!!
//    val refreshState = switchMap(repoResult, { it.refreshState })!!
//
//    fun refresh() {
//        repoResult.value?.refresh?.invoke()
//    }
//
//    fun showSubreddit(subreddit: String): Boolean {
//        if (subredditName.value == subreddit) {
//            return false
//        }
//        subredditName.value = subreddit
//        return true
//    }
//
//    fun retry() {
//        val listing = repoResult?.value
//        listing?.retry?.invoke()
//    }
//
//    fun currentSubreddit(): String? = subredditName.value
//}