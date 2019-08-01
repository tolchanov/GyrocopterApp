package com.test.hyrocoptertestapp.data

import android.os.Environment
import com.test.hyrocoptertestapp.model.InputModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

interface ILogDataManager {
    fun initLogging()
    fun getFileList() : MutableList<String>
    fun log(data: String)
    fun log(data: InputModel)
    fun endLog()
    fun saveLog() : Boolean
}

class LogDataManager(private val headers: Array<String>) : ILogDataManager {
    companion object{
        val TEMP_DIR = "${Environment.getExternalStorageDirectory().path}/Gyrocopter/Temp"
        val LOG_DIR = "${Environment.getExternalStorageDirectory().path}/Gyrocopter/Logs"
    }

    private var tempLogFile: File? = null
    private var tempOS: FileOutputStream? = null


    override fun initLogging() {
        Timber.d("Initializing logging...")
        createTempLogFile()
        Timber.d("Logging initialized!")
    }

    private fun createTempLogFile() {
        val dir = File(TEMP_DIR)
        if(!dir.exists()) dir.mkdirs()
        tempLogFile = File(TEMP_DIR, "temp_feed.csv")
        if(tempLogFile?.exists()==true){
            tempLogFile?.delete()
        }
        tempOS = tempLogFile?.outputStream()
        val sb = StringBuilder()
        headers.forEachIndexed { ind, str ->
            sb.append(str)
            if(ind < headers.size -1) {
                sb.append(", ")
            }
        }
        Timber.e(sb.toString())
        tempOS?.write("${sb.toString()}\n".toByteArray())
    }

    override fun log(data: InputModel){
        tempOS?.write("$data\n".toByteArray())
    }

    override fun log(data: String){
        tempOS?.write("$data\n".toByteArray())
    }

    override fun endLog(){
        Timber.d("Stopping logging...")
        tempOS?.flush()
        tempOS?.close()
        Timber.d("Logging stopped!")
    }

    override fun saveLog(): Boolean {
        return try {
            val dir = File(LOG_DIR)
            if(!dir.exists()) dir.mkdirs()
            if(tempLogFile != null){
                tempLogFile!!.copyTo(File(LOG_DIR, "LOG_${SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault()).format(Calendar.getInstance(Locale.getDefault()).time)}.txt"))
                true
            } else {
                false
            }
        } catch (e: Exception){
            Timber.e(e)
            false
        }
    }

    override fun getFileList() : MutableList<String>{
        val dir = File(LOG_DIR)
        if(!dir.exists()) return mutableListOf()
        return dir.listFiles().filter { it.extension in arrayOf("csv", "txt") }.map{ it.name }.toMutableList()
    }
}