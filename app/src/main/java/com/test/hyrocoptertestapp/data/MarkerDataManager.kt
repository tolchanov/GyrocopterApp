package com.test.hyrocoptertestapp.data

import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.MarkerGenerator
import timber.log.Timber

interface IMarkerDataManager{
    fun initializeMap(googleMap: GoogleMap,click: (Marker, Int) -> Unit)
    fun initializeMarkersAndPolyline(list: MutableList<InputModel>)
    fun addMarker(data: InputModel)
    fun moveTo(index: Int)
    fun drawArrowMarker(pos: Int, data: InputModel)
}

class MarkerDataManager : IMarkerDataManager {
    companion object {
        const val DRAW_ZOOM = 17.3f
    }

    private var googleMap: GoogleMap? = null
    private val markersData = mutableListOf<MarkerOptions>()
    private val markers = mutableListOf<Marker>()
    private var polyline: Polyline? = null
    private lateinit var polylineOptions: PolylineOptions

    private var arrowMarker: Marker? = null

    private var markersShowed = false

    override fun initializeMap(googleMap: GoogleMap, click: (Marker, Int) -> Unit) {
        Timber.d("Map initializing")
        this.googleMap = googleMap
        markers.clear()
        markersData.clear()
        polylineOptions = PolylineOptions().clickable(false).color(Color.parseColor("#dadada")).width(3f).jointType(JointType.ROUND)
        initializeMapListeners(click)
    }

    override fun initializeMarkersAndPolyline(list: MutableList<InputModel>) {
        Timber.d("Markers nad polyline initializing")
        markersData.clear()
        if(list.isNotEmpty()) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(list.first().getLatLng(), 15.3f))
        }
        for(element in list){
            markersData.add(MarkerGenerator.generateMarker(element))
            polylineOptions.add(element.getLatLng())
        }
        polyline?.remove()
        googleMap?.addPolyline(polylineOptions)
    }

    override fun addMarker(data: InputModel) {
        if(markersData.isEmpty()){
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(data.getLatLng(), 16f))
        }
        markersData.add(MarkerGenerator.generateMarker(data))
        googleMap?.also { map->
            if(map.cameraPosition.zoom >= DRAW_ZOOM) {
                markers.add(map.addMarker(markersData.last()))
            }
        }
        polylineOptions.add(data.getLatLng())
        polyline?.remove()
        googleMap?.addPolyline(polylineOptions)
    }

    override fun moveTo(index: Int) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(markersData[index].position))
    }

    override fun drawArrowMarker(pos: Int, data: InputModel) {
        arrowMarker?.remove()
        arrowMarker = googleMap?.addMarker(MarkerGenerator.generateArrowMarker(data))
    }

    private fun initializeMapListeners(click: (Marker, Int) -> Unit){
        googleMap?.also { map ->
            map.setOnCameraMoveListener {
                if(!markersShowed && map.cameraPosition.zoom >= DRAW_ZOOM){
                    Timber.d("Markers showed!")
                    markersShowed = true
                    for(opt in markersData){
                        markers.add(map.addMarker(opt))
                    }
                } else if(markersShowed && map.cameraPosition.zoom < DRAW_ZOOM){
                    Timber.d("Markers hidden!")
                    markersShowed = false
                    for(marker in markers){
                        marker.remove()
                    }
                    markers.clear()
                }
            }

            map.setOnMarkerClickListener { marker ->
                if(marker == arrowMarker) return@setOnMarkerClickListener true
                click(marker, markers.indexOf(marker))
                true
            }
        }
    }
}