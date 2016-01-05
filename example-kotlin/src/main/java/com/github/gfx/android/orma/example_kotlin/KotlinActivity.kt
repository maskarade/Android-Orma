/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gfx.android.orma.example_kotlin

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.example_kotlin.databinding.ActivityKotlinBinding
import com.github.gfx.android.orma.example_kotlin.databinding.ItemBinding
import com.github.gfx.android.orma.widget.OrmaListAdapter

class KotlinActivity : AppCompatActivity() {

    lateinit var binding: ActivityKotlinBinding

    lateinit var orma: OrmaDatabase

    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_kotlin)

        orma = OrmaDatabase.builder(this).build()
        adapter = Adapter(this, orma.relationOfItem().orderByIdAsc())
        binding.list.adapter = adapter

        binding.fab.setOnClickListener {
            Log.d("XXX", "hoge")
            adapter.addItemAsObservable({
                val item = Item()
                item.content = "content #" + orma.selectFromItem().count()
                item
            }).subscribe({
                Toast.makeText(this, "item created!", Toast.LENGTH_SHORT).show()
            })
        }
    }

    class Adapter(context: Context?, relation: Relation<Item, *>?) : OrmaListAdapter<Item>(context, relation) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var binding: ItemBinding
            if (convertView == null) {
                binding = DataBindingUtil.inflate(layoutInflater, R.layout.item, parent, false)
            } else {
                binding = DataBindingUtil.getBinding(convertView)
            }

            val item = getItem(position)
            binding.text.text = item.content;

            binding.root.setOnClickListener {
                removeItemAsObservable(item).subscribe()
            }

            return binding.root;
        }
    }
}
