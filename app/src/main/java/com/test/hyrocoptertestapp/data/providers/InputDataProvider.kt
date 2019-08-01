package com.test.hyrocoptertestapp.data.providers

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.test.hyrocoptertestapp.model.InputModel
import com.test.hyrocoptertestapp.utils.PlaybackState
import com.test.hyrocoptertestapp.utils.ProcessStatus

interface InputDataProvider {
    val inputData : MutableLiveData<InputModel>
    val preparing: MutableLiveData<ProcessStatus>
    val playback: MutableLiveData<PlaybackState>

    @Throws(IllegalArgumentException::class)
    fun prepare(prepareData: Bundle? = null)
    fun play(toNext: Boolean)
    fun stop()
    fun getList(): MutableList<InputModel>
    fun setTrackedPosition(pos: Int)
}