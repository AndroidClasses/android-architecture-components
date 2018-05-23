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

package com.android.example.paging.pagingwithnetwork

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.example.paging.pagingwithnetwork.base.repository.BasePostRepository.Type
import com.android.example.paging.pagingwithnetwork.reddit.ui.RedditActivity
import com.android.example.paging.pagingwithnetwork.skin.ui.SkinActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * chooser activity for the demo.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        withDatabase.setOnClickListener {
            show(Type.DB)
        }
        networkOnly.setOnClickListener {
            show(Type.IN_MEMORY_BY_ITEM)
        }
        networkOnlyWithPageKeys.setOnClickListener {
            show(Type.IN_MEMORY_BY_PAGE)
        }

        skinPageKeys.setOnClickListener {
            showSkinPage()
        }
    }

    private fun showSkinPage() {
        val intent = SkinActivity.intentFor(this)
        startActivity(intent)
    }

    private fun show(type: Type) {
        val intent = RedditActivity.intentFor(this, type)
        startActivity(intent)
    }
}
