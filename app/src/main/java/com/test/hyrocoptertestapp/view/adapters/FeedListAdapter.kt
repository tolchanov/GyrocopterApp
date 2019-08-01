package com.test.hyrocoptertestapp.view.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.toPosixDate

class FeedListAdapter(private val data: MutableList<InputModel> = mutableListOf(), private val listener: FeedListListener) : RecyclerView.Adapter<FeedListAdapter.ViewHolder>() {

    var isTracked = true

    private var activeItem = 0


    fun activateItem(item: InputModel) {
        val index = data.indexOf(item)
        if (index != -1) {
            activateItem(index)
        }
    }

    fun addItem(item: InputModel) {
        data.add(item)
        notifyItemRangeInserted(data.size - 1, 1)
        activateItem()
        listener.onItemInserted(item)
    }

    fun activateItem(itemPosition: Int = -1) {
        val prev = activeItem
        if (itemPosition == -1) {
            activeItem = data.size - 1
        } else {
            if (itemPosition in data.indices) {
                activeItem = itemPosition
            } else {
                throw IndexOutOfBoundsException("itemPosition not in data indices range! $itemPosition - pos, ${data.size} - size")
            }
        }
        listener.onActiveItemChanged(activeItem, prev, data[activeItem])
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setPadding(0, 8, 0, 8)
            }
        })
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(data[position], position, activeItem)
        holder.itemView.setOnClickListener { listener.onItemClick(position, data[position]) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(data: InputModel, position: Int, activeItem: Int) {

            (itemView as LinearLayout).also { container ->
                container.removeAllViews()
                val pattern = itemView.context.getString(R.string.double_pattern)
                container.addView(buildTextView((position+1).toString()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                        weight = 0.5f
                    }
                })
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(data.gpsTime.toPosixDate()))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView((data.lat/10000000.0).toString()))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView((data.lon/10000000.0).toString()))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.alt)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.heading)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.speed)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.range)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.bearing)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.autopilotDir)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.angleError)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.imuYaw)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.imuPitch)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.imuRoll)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(pattern.format(data.barAlt)))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(data.steering.toString()))
                container.addView(buildVerticalDivider())
                container.addView(buildTextView(data.mode))
            }

            if (position < activeItem) {
                itemView.background = itemView.context.getDrawable(R.drawable.bg_feed_prev)
            } else if (position == activeItem) {
                itemView.background = itemView.context.getDrawable(R.drawable.bg_feed_active)
            } else {
                itemView.background = itemView.context.getDrawable(R.drawable.bg_feed_next)
            }
        }

        private fun buildTextView(data: String): TextView{
            return TextView(itemView.context).apply {
                text = data
                maxLines = 3
                textSize = 10f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    weight = 1f
                }
//                TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            }
        }

        private fun buildVerticalDivider():View{
            return View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
                background = ColorDrawable(Color.WHITE)
            }
        }

    }
}

interface FeedListListener {
    fun onItemClick(position: Int, data: InputModel)
    fun onActiveItemChanged(position: Int, prev: Int, data: InputModel)
    fun onItemInserted(data: InputModel)
}