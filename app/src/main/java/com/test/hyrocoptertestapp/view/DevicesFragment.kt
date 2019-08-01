package com.test.hyrocoptertestapp.view

import android.app.AlertDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.utils.*
import com.test.hyrocoptertestapp.view.adapters.DeviceListAdapter
import com.test.hyrocoptertestapp.viewmodel.MainViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.device_list_header.view.*
import kotlinx.android.synthetic.main.fragment_device_list.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.w3c.dom.Text
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class DevicesFragment : Fragment() {

    private val viewModel by sharedViewModel<MainViewModel>()

    private var baudRate = 57600
    private val listAdapter: DeviceListAdapter = DeviceListAdapter(mutableListOf()){ item ->
        if(item.driver == null){
            context?.toast("no driver")
        } else {
            val args = Bundle()
            args.putBoolean(BUNDLE_IS_USB, true)
            args.putInt(BUNDLE_DEVICE_ID, item.device.deviceId)
            args.putInt(BUNDLE_PORT, item.port)
            args.putInt(BUNDLE_BAUD_RATE, baudRate)
            viewModel.deviceListClick.callWithValue(args)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragment_device_list_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        fragment_device_list_recycler.adapter = listAdapter

        fragment_device_list_header.header_refresh.setOnClickListener {
            viewModel.refreshDeviceList()
        }
        fragment_device_list_header.header_baud_rate.setOnClickListener {
            val baudRates = resources.getStringArray(R.array.baud_rates)
            val pos = baudRates.indexOf(baudRate.toString())
            AlertDialog.Builder(context)
                    .setTitle(R.string.baud_title)
                    .setSingleChoiceItems(baudRates, pos){ dialog, it ->
                        baudRate = baudRates[it].toInt()
                        dialog.dismiss()
                    }.create().show()
        }
        fragment_device_list_header.header_storage.setOnClickListener {
            AlertDialog.Builder(context)
                        .setAdapter(ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, viewModel.getFileList())){ a, b ->
                            val args = Bundle()
                            args.putBoolean(BUNDLE_IS_USB, false)
                            args.putString(BUNDLE_FILENAME, viewModel.getFileList()[b])
                            viewModel.deviceListClick.callWithValue(args)
                        }.show()
        }

        viewModel.deviceListData.observe(viewLifecycleOwner, Observer {
            listAdapter.setData(it)
            if(it.size > 0){
                fragment_device_list_empty.visibility = View.GONE
            } else {
                fragment_device_list_empty.visibility = View.VISIBLE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceList()
    }
}