package com.test.hyrocoptertestapp.data.providers

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.test.hyrocoptertestapp.data.LogDataManager.Companion.LOG_DIR
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

class LogDataProvider : InputDataProvider {

    override val preparing: SingleLiveEvent<ProcessStatus> = SingleLiveEvent()
    override val inputData: MutableLiveData<InputModel> = MutableLiveData()
    override val playback: MutableLiveData<PlaybackState> = MutableLiveData<PlaybackState>().apply { value = PlaybackState.PAUSED }

    private val dataList = mutableListOf<InputModel>()
    private var disposable: Disposable? = null
    private var lastElement = 0
    private var direction = 1 // 1 to next, -1 to prev


    override fun prepare(prepareData: Bundle?) {
        if(prepareData != null) {
            GlobalScope.launch {
                dataList.clear()
                if(openFile(prepareData.getString(BUNDLE_FILENAME))) {
                    lastElement = 0
                }
            }
        } else {
            throw IllegalArgumentException("prepare data must be not null!")
        }
    }

    override fun stop() {
        Timber.e("1 sec timer disposed!")
        disposable?.dispose()
        disposable = null
        playback.value = PlaybackState.PAUSED
    }

    override fun play(toNext: Boolean) {
        if(disposable != null) return
        direction = if(toNext) 1 else -1
        disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    provideNewData()
                }, {
                    Timber.e(it)
                })
        playback.value = PlaybackState.PLAYING
    }

    override fun getList(): MutableList<InputModel> {
        return dataList
    }

    override fun setTrackedPosition(pos: Int) {
        lastElement = pos
    }

    private fun provideNewData(){
        lastElement+=direction
        if(lastElement < -1) lastElement = 0
        else if (lastElement > dataList.size) lastElement = dataList.size-1
        if(!isAtTheEnd()) {
            inputData.value = dataList[lastElement]
        } else {
            stop()
        }
    }

    private fun isAtTheEnd() = (direction == -1 && lastElement == -1) || (direction == 1 && lastElement == dataList.size)

    private fun openFile(filename: String?) : Boolean{
        preparing.postCall(ProcessStatus(Status.LOADING))
        return try {
            Timber.e("Opening file...")
            val file = File(LOG_DIR, filename)
            if (!file.exists()) throw FileNotFoundException("File ${file.path} doesn't exists")
            openCSV(file)
            preparing.postCall(ProcessStatus(Status.SUCCESS))
            true
        } catch (e: Exception){
            Timber.e(e)
            preparing.postCall(ProcessStatus(Status.ERROR, e.message))
            false
        }
    }

    private fun openCSV(file: File){
        Timber.e("Opening .csv...")
        val reg = Regex("[\\-?\\d+\\.?\\d?,\\s?]{15,}\\s?,\\s?\\D+")
        val fis = FileInputStream(file)
        val br = fis.bufferedReader()
        var s: String? = br.readLine()
        while (s!= null){
            if(reg.matches(s)) {
                dataList.add(s.getInputModel())
            }
            s = br.readLine()
        }
        br.close()
        fis.close()
    }
}