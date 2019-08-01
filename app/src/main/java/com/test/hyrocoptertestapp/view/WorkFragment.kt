package com.test.hyrocoptertestapp.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.*
import com.test.hyrocoptertestapp.view.adapters.FeedListAdapter
import com.test.hyrocoptertestapp.view.adapters.FeedListListener
import com.test.hyrocoptertestapp.viewmodel.MainViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_work.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class WorkFragment : Fragment(), OnMapReadyCallback {
    companion object {
        fun newInstance(arguments: Bundle) = WorkFragment().apply {
            this.arguments = arguments
        }
    }

    private lateinit var mMap: GoogleMap
    private var isUsb = false
    private var feedAdapter: FeedListAdapter? = null

    private val viewModel by sharedViewModel<MainViewModel>()

    private var acceptionCounter = 0
    private lateinit var tts: TextToSpeech


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.startSyncOfMapAndData()
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        val mapFragment = childFragmentManager.findFragmentById(R.id.work_map_frame) as SupportMapFragment
        mapFragment.getMapAsync(this)
        arguments?.also { data ->
            isUsb = data.getBoolean(BUNDLE_IS_USB)
            viewModel.initProvider(isUsb)
            viewModel.prepare(data)
        }

        initTtS()
        viewModel.preparingData?.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    work_progress.visibility = View.VISIBLE
                }
                Status.SUCCESS -> {
                    work_progress.visibility = View.GONE
                    initAdapter()
                    Timber.e("Provider success!")
                    viewModel.logLoaded?.offer(true)
                    if(isUsb) {
                        viewModel.startLogging()
                    }
                }
                Status.ERROR -> {
                    work_progress.visibility = View.GONE
                    context?.toast(it.message)
                }
            }
        })
        viewModel.providedData?.observe(viewLifecycleOwner, Observer {
            if (it is InputModel) {
                if (isUsb) {
                    feedAdapter?.addItem(it)
                    try {
                        viewModel.log(it)
                    } catch (e: Exception){
                        Timber.e("Logging failed! ${e.message}")
                    }
                    acceptionCounter++
                    if(acceptionCounter == 5){
                        acceptionCounter = 0
                        tts.speak("${it.alt}", TextToSpeech.QUEUE_FLUSH, null)
                    }
                } else {
                    feedAdapter?.activateItem(it)
                }
            }
        })

        viewModel.playback?.observe(viewLifecycleOwner, Observer {
            when (it) {
                PlaybackState.PAUSED -> {
                    work_play_pause.isEnabled = false
                    work_play_next.isEnabled = true
                    work_play_prev.isEnabled = true
                }
                PlaybackState.PLAYING -> {
                    work_play_pause.isEnabled = true
                    work_play_prev.isEnabled = false
                    work_play_next.isEnabled = false
                }
                else -> {
                }
            }
        })

        work_play_pause.isEnabled = false

        if (isUsb) {
            work_navigation.visibility = View.GONE
            work_save_button.setOnClickListener {
                try {
                    viewModel.stopLogging()
                    if (viewModel.saveLog()) {
                        context?.toast("Log saved!")
                        viewModel.startLogging()
//                        work_save_button.isEnabled = false
//                        work_save_button.visibility = View.GONE
                    }
                } catch (e: Exception){
                    context?.toast("Saving failed!")
                    Timber.e(e)
                }
            }
            work_clear_button.setOnClickListener {
                try{
                    viewModel.stopLogging()
                    viewModel.startLogging()
                } catch (e: Exception){
                    Timber.e(e)
                }
            }
        } else {
            work_save_button.visibility = View.GONE
            work_clear_button.visibility = View.GONE
            work_play_pause.setOnClickListener {
                viewModel.stop()
            }
            work_play_prev.setOnClickListener {
                viewModel.play(false)
            }
            work_play_next.setOnClickListener {
                viewModel.play(true)
            }
        }
        initDragger()

        viewModel.allLoaded.observe(viewLifecycleOwner, Observer {
            Timber.e("Map and provider loaded! it = $it")
            viewModel.dispose()
            drawOnMap()
        })

        work_track_button.visibility = View.INVISIBLE
    }


    private fun initView(){
        work_speed_label.text = getString(R.string.pattern_speed, 0.0)
        work_compass_label.text = getString(R.string.pattern_degrees, 0.0)
        work_steering_label.text = "0"
        work_altitude_label.text = getString(R.string.double_pattern, 0.0)
        work_base.text = getString(R.string.pattern_meters, 0.0)
        work_mode.text = getString(R.string.default_mode)
    }

    private fun updateView(data: InputModel) {
        work_speed.progress = (data.speed * 10).toInt()
        work_speed_label.text = getString(R.string.pattern_speed, data.speed)
        work_compass.degrees = data.heading
        work_compass_label.text = getString(R.string.pattern_degrees, data.heading)
        work_steering.progress = data.steering.boundTo(1000)
        work_steering_label.text = "${data.steering}"
        work_altitude.progress = data.barAlt.toInt().boundTo(360)
        work_altitude_label.text = getString(R.string.double_pattern, data.barAlt)
        work_base.text = getString(R.string.pattern_meters, data.range)
        work_mode.text = data.mode
    }

    private var circle: Circle? = null


    private fun initTtS(){
        tts = TextToSpeech(context?.applicationContext){
            if(it!= TextToSpeech.ERROR){
                tts.setLanguage(Locale.US)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.uiSettings.isZoomControlsEnabled = true
        Timber.e("Map success!")
        viewModel.mapReady?.offer(true)
        viewModel.initMap(map){ marker, pos ->
            stopTracking()
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            feedAdapter?.activateItem(pos)
            if(!isUsb) {
                viewModel.setTrackedPosition(pos)
            }
            viewModel.getList()?.also {
                drawMarkerArrows(pos, it[pos])
                updateView(it[pos])
            }
        }

        work_circle_button.setOnClickListener {
            if (!work_circle_lat.text.isEmpty() && !work_circle_lng.text.isEmpty() && !work_circle_radius.text.isEmpty()) {
                try {
                    circle?.remove()
                    val circleOpt = CircleOptions()
                            .center(LatLng(work_circle_lat.text.toString().toDouble(), work_circle_lng.text.toString().toDouble()))
                            .radius(work_circle_radius.text.toString().toDouble())
                            .strokeColor(Color.RED)
                            .fillColor(Color.parseColor("#55ff0000"))
                            .strokeWidth(3f)
                    circle = mMap.addCircle(circleOpt)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mMap.setOnMapLongClickListener {
            work_circle_lat.setText(it.latitude.toString())
            work_circle_lng.setText(it.longitude.toString())
        }

        work_map_type_button.setOnClickListener {
            if(mMap.mapType == GoogleMap.MAP_TYPE_NORMAL){
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                work_map_type_button.setImageResource(R.drawable.ic_map)
            } else {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                work_map_type_button.setImageResource(R.drawable.ic_satellite)
            }
        }
    }


    private fun initDragger() {
        val out = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(out)
        val w = out.widthPixels.toFloat() * 0.95f - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, out)
        work_card.x = w
        work_dragger.x = w - work_dragger.width
        work_track_button.x = w - work_track_button.width
        work_dragger.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    work_card.translationX = event.rawX - work_dragger.width
                    work_dragger.translationX = event.rawX - work_dragger.width
                    work_track_button.translationX = event.rawX - work_dragger.width
                }
                MotionEvent.ACTION_UP -> {
                    glueCard()
                }
            }
            true
        }
        work_track_button.setOnClickListener {
            feedAdapter?.isTracked = true
            work_track_button.visibility = View.INVISIBLE
        }
    }

    private fun glueCard() {
        val out = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(out)
        val mapX = work_scroll.x.toInt()
        val navX = mapX + work_scroll.measuredWidth / 2
        when (work_card.x.toInt()) {
            in (0..mapX / 2) -> {
                work_card.animate().x(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, out) + work_dragger.width).setDuration(300).start()
                work_dragger.animate().x(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, out)).setDuration(300).start()
                work_track_button.animate().x(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, out)).setDuration(300).start()
            }
            in (mapX / 2..navX) -> {
                work_card.animate().x(mapX.toFloat()).setDuration(300).start()
                work_dragger.animate().x(mapX.toFloat() - work_dragger.width).setDuration(300).start()
                work_track_button.animate().x(mapX.toFloat() - work_track_button.width).setDuration(300).start()
            }
            else -> {
                work_card.animate().x(out.widthPixels.toFloat() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, out)).setDuration(300).start()
                work_dragger.animate().x(out.widthPixels.toFloat() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, out) - work_dragger.width).setDuration(300).start()
                work_track_button.animate().x(out.widthPixels.toFloat() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, out) - work_dragger.width).setDuration(300).start()
            }
        }
    }

    private fun initHeader() {
        val headers = resources.getStringArray(R.array.header)
        work_card_header.removeAllViews()
        work_card_header.addView(TextView(context).apply {
            text = "#"
            textSize = 10f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.5f
            }
        })
        work_card_header.addView(View(context).apply {
            layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
            background = ColorDrawable(Color.WHITE)
        })
        headers.forEachIndexed { index, header ->
            work_card_header.addView(TextView(context).apply {
                text = header
                textSize = 10f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    weight = 1f
                }
            })
            if (index != headers.size - 1) {
                work_card_header.addView(View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
                    background = ColorDrawable(Color.WHITE)
                })
            }
        }
    }

    private fun initAdapter() {
        initHeader()
        work_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val data = viewModel.getList()
        if (data != null) {
            feedAdapter = FeedListAdapter(if(isUsb) mutableListOf<InputModel>().apply{ addAll(data) } else data, object : FeedListListener {
                override fun onItemClick(position: Int, data: InputModel) {
                    stopTracking()
                    feedAdapter?.activateItem(position)
                    updateView(data)
                    if (!isUsb) {
                        viewModel.setTrackedPosition(position)
                        viewModel.moveToMarker(position)
                        drawMarkerArrows(position, data)
                    }
                }

                override fun onActiveItemChanged(position: Int, prev: Int, data: InputModel) {
                    val lm = (work_recycler.layoutManager as? LinearLayoutManager)
                    if (feedAdapter != null && lm != null) {
                        val out = DisplayMetrics()
                        activity?.windowManager?.defaultDisplay?.getMetrics(out)
                        if (position > prev) {
                            feedAdapter?.notifyItemRangeChanged(prev, position - prev + 1)
                        } else {
                            feedAdapter?.notifyItemRangeChanged(position, lm.findLastVisibleItemPosition() - position)
                        }
                        if (feedAdapter?.isTracked == true) {
                            lm.scrollToPositionWithOffset(position, (out.heightPixels / 2.5).toInt())
                            drawMarkerArrows(position, data)
                            updateView(data)
                        }
                    }
                }

                override fun onItemInserted(data: InputModel) {
                    viewModel.addMarker(data)
                }
            })
        }
        work_recycler.adapter = feedAdapter
        work_recycler.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                stopTracking()
            }
            false
        }
        work_recycler.setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 20)
        })
    }

    private fun stopTracking(){
        feedAdapter?.isTracked = false
        work_track_button.visibility = View.VISIBLE
        work_track_button.x = work_card.x - work_track_button.width
    }

    private fun drawOnMap() {
        val data = viewModel.getList()
        if(data != null) {
            viewModel.initMarkers(data)
        }
    }

    private fun drawMarkerArrows(pos: Int, data: InputModel){
        viewModel.drawMarkerArrows(pos, data)
    }


    override fun onDestroyView() {
        viewModel.stop()
        viewModel.dispose()
        super.onDestroyView()
    }
}