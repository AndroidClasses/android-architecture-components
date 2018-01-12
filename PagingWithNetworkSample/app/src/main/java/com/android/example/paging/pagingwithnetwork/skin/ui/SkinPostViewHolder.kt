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

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.example.paging.pagingwithnetwork.R
import com.android.example.paging.pagingwithnetwork.skin.vo.SkinPost
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

/**
 * A RecyclerView ViewHolder that displays a skin post.
 */
class SkinPostViewHolder(view: View, private val glide: RequestManager)
    : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val score: TextView = view.findViewById(R.id.score)
    private val thumbnail : ImageView = view.findViewById(R.id.thumbnail)
    private var post : SkinPost? = null
    init {
        view.setOnClickListener {
            post?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(post: SkinPost?) {
        this.post = post
        title.text = post?.title ?: "loading"
        subtitle.text = itemView.context.resources.getString(R.string.post_subtitle,
                post?.name ?: "unknown")
        score.text = "${post?.name ?: 0}"
        if (post?.url?.startsWith("http") == true) {
            thumbnail.visibility = View.VISIBLE
            glide.load(post.url).centerCrop()
                    .placeholder(R.drawable.ic_insert_photo_black_48dp)
                    .into(thumbnail)
        } else {
            thumbnail.visibility = View.GONE
            Glide.clear(thumbnail)
        }

    }

    companion object {
        fun create(parent: ViewGroup, glide: RequestManager): SkinPostViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.reddit_post_item, parent, false)
            return SkinPostViewHolder(view, glide)
        }
    }

    fun updateScore(item: SkinPost?) {
        post = item
        score.text = "${item?.name ?: 0}"
    }
}