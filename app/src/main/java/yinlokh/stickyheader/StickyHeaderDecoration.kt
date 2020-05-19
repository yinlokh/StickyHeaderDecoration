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

package yinlokh.stickyheader

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.HashSet

/**
 * ItemDecoration for adding custom views as sticky headers.  Headers are recycled using
 * [RecyclerView.ViewHolder] and stored in [RecyclerView.RecycledViewPool].  Headers are drawn
 * directly to canvas using [View.draw] and do not behave as normal views.
 */
class StickyHeaderDecoration : RecyclerView.ItemDecoration() {

    val indexToPos : MutableMap<Int, Int> = HashMap()
    val posToIndex : MutableMap<Int, Int> = HashMap()
    val headerViewPool : RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    val headerViewHolders : MutableMap<Int, RecyclerView.ViewHolder>  = HashMap()

    var headerAdapter : HeaderAdapter? = null
        set(value) {
            field = value
            computeHeaderIndicies()
        }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        ensureViewHolders(parent)

        var highestHeaderTop = Float.MAX_VALUE

        // draw headers from bottom up, since each header boundary determine max bottom of the next
        for (i in parent.childCount - 1 downTo 0) {
            var child = parent.getChildAt(i)
            var pos = parent.getChildLayoutPosition(child)
            if (indexToPos.values.contains(pos)) {
                val ind = posToIndex.get(pos)
                val margin = (child.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
                val headerHeight = (headerViewHolders[ind]?.itemView?.height ?: 0)
                var translateY = Math.max(0f, child.y - margin - headerHeight)
                if (translateY + headerHeight > highestHeaderTop) {
                    translateY = highestHeaderTop - headerHeight
                }

                highestHeaderTop = Math.min(highestHeaderTop, translateY)
                c.save()
                c.translate(0f, translateY)
                headerViewHolders[ind]?.itemView?.draw(c)
                c.restore()
            }
        }

        // draw any remaining sticky header if the first header is not topped out
        if (highestHeaderTop > 0) {
            var first = parent.getChildLayoutPosition(parent.getChildAt(0))
            var stickyHeaderPos = posToIndex.keys.filter { it < first }.sortedDescending().first()
            var stickyHeaderInd = posToIndex.get(stickyHeaderPos)
            var headerHeight = (headerViewHolders[stickyHeaderInd]?.itemView?.height?.toFloat() ?: 0f)
            var stickyHeaderOffset = highestHeaderTop - headerHeight
            stickyHeaderOffset = Math.min(stickyHeaderOffset, 0f)
            c.save()
            c.translate(0f, stickyHeaderOffset)
            headerViewHolders[stickyHeaderInd]?.itemView?.draw(c)
            c.restore()
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val pos = parent.getChildAdapterPosition(view)
        ensureViewHolders(parent)
        if (indexToPos.values.contains(pos)) {
            val ind = posToIndex.get(pos)
            outRect.top = headerViewHolders[ind]?.itemView?.measuredHeight ?: 0
        }
    }

    private fun ensureViewHolders(parent: RecyclerView) {
        if (parent.layoutManager !is LinearLayoutManager) {
         return
        }

        val linearLayoutManager = parent.layoutManager as LinearLayoutManager
        val firstVisible = linearLayoutManager.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager.findLastVisibleItemPosition()
        var unmappedHeaders = HashSet<Int>()
        var stickyHeaderCandidate = -1
        for (header in indexToPos.keys) {
            val headerPosition = indexToPos.get(header) ?: 0
            if (headerPosition < firstVisible) {
                stickyHeaderCandidate = Math.max(stickyHeaderCandidate, header)
            }
            if (headerPosition >= firstVisible || headerPosition <= lastVisible) {
                unmappedHeaders.add(header)
            }
        }
        unmappedHeaders.add(stickyHeaderCandidate)

        val recycledHeaders = HashSet<Int>()
        for (header in headerViewHolders.keys) {
            if (unmappedHeaders.contains(header)) {
                unmappedHeaders.remove(header)
            } else {
                val holder = headerViewHolders.get(header)
                headerViewPool.putRecycledView(holder)
                recycledHeaders.add(header)
            }
        }

        recycledHeaders.forEach { headerViewHolders.remove(it) }
//
//        unmappedHeaders.map {
//            headerViewPool.getRecycledView(
//            headerAdapter?.getHeaderType(it)?:0)}.()
//            .forEach()
//            ?: createHeaderViewHolder(parent, header) }
        for (header in unmappedHeaders) {
            val holder = headerViewPool.getRecycledView(
                headerAdapter?.getHeaderType(header)?:0)
                ?: createHeaderViewHolder(parent, header)
            if (holder != null) {
                bindHeaderViewHolder(holder, header, parent)
                headerViewHolders.put(header, holder)
            }
        }
    }

    private fun bindHeaderViewHolder(
        holder: RecyclerView.ViewHolder,
        header: Int,
        parent: RecyclerView
    ) {
        headerAdapter?.onBindHeaderViewHolder(holder, header)
        val view = holder.itemView
        view.measure(
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.AT_MOST)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun createHeaderViewHolder(parent : RecyclerView, header: Int)
            : RecyclerView.ViewHolder? {
        return headerAdapter?.onCreateHeaderViewHolder(
            parent,
            headerAdapter?.getHeaderType(header) ?: 0
        )
    }

    private fun computeHeaderIndicies() {
        posToIndex.clear()
        indexToPos.clear()

        val headerCount = headerAdapter?.getHeaderCount()
        for (header in 0 until (headerCount?:0)) {
            val position = headerAdapter?.getHeaderPosition(header)
            if (position != null) {
                posToIndex.put(position, header)
                indexToPos.put(header, position)
            }
        }
    }

    interface HeaderAdapter {

        fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int)

        fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

        fun getHeaderCount(): Int

        fun getHeaderPosition(header : Int): Int

        fun getHeaderType(header : Int) : Int
    }
}
