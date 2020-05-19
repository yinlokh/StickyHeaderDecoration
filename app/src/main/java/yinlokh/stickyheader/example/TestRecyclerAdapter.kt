/*
 *    Copyright 2020 Yin Lok Ho
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package yinlokh.stickyheader.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import yinlokh.stickyheader.R
import yinlokh.stickyheader.StickyHeaderDecoration

class TestRecyclerAdapter() : RecyclerView.Adapter<TestRecyclerAdapter.TestItemViewHolder>() {

    val decoration : StickyHeaderDecoration =
        StickyHeaderDecoration()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.example_item, parent, false)
        return TestItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 50;
    }

    override fun onBindViewHolder(holder: TestItemViewHolder, position: Int) {
        holder.text.text = position.toString()
    }

    class TestItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val text = itemView.findViewById<TextView>(R.id.text)
    }
}