package com.test.hyrocoptertestapp.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.model.DeviceListItem
import kotlinx.android.synthetic.main.device_list_item.view.*
import java.util.*

class DeviceListAdapter(private val data: MutableList<DeviceListItem>, private val onClick:(DeviceListItem) -> Unit) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    fun setData(data: MutableList<DeviceListItem>){
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.device_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(data[position])
        holder.itemView.setOnClickListener { onClick(data[position]) }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindView(item: DeviceListItem){
            if(item.driver == null){
                itemView.text1.text = "<no driver>"
            } else if (item.driver.ports?.size == 1){
                itemView.text1.text = item.driver::class.java.simpleName.replace("SerialDriver", "")
            } else {
                itemView.text1.text = item.driver.javaClass.simpleName.replace("SerialDriver", "") + ", Port ${item.port}"
            }
            itemView.text2.text = String.format(Locale.US, "Vendor %04X, Product %04X",item.device.vendorId, item.device.productId)
        }
    }
}