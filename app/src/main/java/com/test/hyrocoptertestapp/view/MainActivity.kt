package com.test.hyrocoptertestapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.utils.DISPLAY_METRICS
import com.test.hyrocoptertestapp.utils.toast
import com.test.hyrocoptertestapp.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_device_list.*
import org.koin.androidx.viewmodel.ext.android.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment, DevicesFragment(), DevicesFragment::class.java.simpleName).commit()
        }
        viewModel.deviceListClick.observe(this, Observer {
            startFragment(WorkFragment.newInstance(it))
            Timber.e("observe device click")
        })

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 111)
        }

        windowManager?.defaultDisplay?.getMetrics(DISPLAY_METRICS)
    }

    fun startFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().add(R.id.fragment, fragment, fragment::class.java.simpleName).addToBackStack(null).commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111){
            if(grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                toast("No logging available!")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
