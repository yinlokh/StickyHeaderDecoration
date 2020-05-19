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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.example_header.view.*
import yinlokh.stickyheader.R
import yinlokh.stickyheader.StickyHeaderDecoration


class MainActivity : AppCompatActivity() {

    val adapter = TestRecyclerAdapter()
    val decoration = StickyHeaderDecoration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(decoration)
        decoration.headerAdapter = object :
            StickyHeaderDecoration.HeaderAdapter {
            override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder.itemView.text.text = "HEADER " + position
            }

            override fun onCreateHeaderViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val layout = when (viewType){
                    0 -> R.layout.example_header
                    1 -> R.layout.example_header2
                    2 -> R.layout.example_header3
                    else -> R.layout.example_header
                }

                val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
                return Holder(view)
            }

            override fun getHeaderCount(): Int {
              return 10
            }

            override fun getHeaderPosition(header: Int): Int {
              return header * 3
            }

            override fun getHeaderType(header: Int): Int {
                return header % 3;
            }
        }
    }

    class Holder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.text
    }
}
