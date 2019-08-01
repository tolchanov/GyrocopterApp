package com.test.hyrocoptertestapp.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.test.hyrocoptertestapp.data.ILogDataManager
import com.test.hyrocoptertestapp.data.IMarkerDataManager
import com.test.hyrocoptertestapp.data.IUsbDataManager
import com.test.hyrocoptertestapp.data.providers.InputDataProvider
import com.test.hyrocoptertestapp.data.providers.InputDataProviderFactory
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.lang.Exception

class MainViewModel(private val markerDataManager: IMarkerDataManager,
                    private val usbDataManager: IUsbDataManager,
                    private val logDataManager: ILogDataManager) : ViewModel() {


    private var dataProvider: InputDataProvider? = null

    var providedData: MutableLiveData<InputModel>? = null
        private set
    var preparingData: MutableLiveData<ProcessStatus>? = null
        private set
    var playback: MutableLiveData<PlaybackState>? = null
        private set

    var mapReady: PublishProcessor<Boolean>? = null
    var logLoaded: PublishProcessor<Boolean>? = null
    val allLoaded: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private var onAllReady: Disposable? = null

    fun startSyncOfMapAndData(){
        Timber.e("Start syncing!")
        mapReady = PublishProcessor.create<Boolean>()
        logLoaded = PublishProcessor.create<Boolean>()
        onAllReady = Flowable.combineLatest(mapReady, logLoaded, BiFunction<Boolean, Boolean, Boolean> { t1, t2 ->
            Timber.e("SYNCHRONIZER: t1 = $t1, t2 = $t2, t1&t2 = ${t1&&t2}")
            t1 && t2
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    allLoaded.value = it
                }, {
                    Timber.e(it)
                })
    }
    // logging

    fun getFileList() = logDataManager.getFileList()
    fun startLogging() = logDataManager.initLogging()
    fun log(data: InputModel) = logDataManager.log(data)
    fun saveLog(): Boolean = logDataManager.saveLog()
    fun stopLogging() = logDataManager.endLog()


    // data providing
    fun initProvider(isUsb: Boolean) {
        dataProvider = InputDataProviderFactory.getInputDataProvider(isUsb)
        providedData = dataProvider?.inputData
        preparingData = dataProvider?.preparing
        playback = dataProvider?.playback
        Timber.d("Provider initialized")
    }

    fun prepare(bundle: Bundle) = dataProvider?.prepare(bundle)
    fun play(toNext: Boolean) = dataProvider?.play(toNext)
    fun stop() {
        dataProvider?.stop()
        try {
            logDataManager.endLog()
        } catch (e: Exception){}
    }

    fun getList() = dataProvider?.getList()
    fun setTrackedPosition(pos: Int) = dataProvider?.setTrackedPosition(pos)

    // map
    fun initMap(map: GoogleMap, click: (Marker, Int) -> Unit) = markerDataManager.initializeMap(map, click)

    fun initMarkers(data: MutableList<InputModel>) = markerDataManager.initializeMarkersAndPolyline(data)
    fun addMarker(data: InputModel) = markerDataManager.addMarker(data)
    fun moveToMarker(pos: Int) = markerDataManager.moveTo(pos)
    fun drawMarkerArrows(pos: Int, data: InputModel) = markerDataManager.drawArrowMarker(pos, data)


    // usb
    val deviceListClick = SingleLiveEvent<Bundle>()
    val deviceListData = usbDataManager.deviceListData
    fun refreshDeviceList() = usbDataManager.refreshDeviceList()

    fun dispose() {
        onAllReady?.dispose()
        Timber.e("Synchronizer disposed!")
    }


    /**
     * Map caching
     */
//    fun getCacheList() = dataManager.getCachedList()
//    val getCacheInfo = {dataManager.getCacheInfoModel()}
//    val loadCacheFile:(String) -> Unit = {dataManager.loadCacheFile(it)}
}
