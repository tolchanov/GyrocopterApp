package com.test.hyrocoptertestapp.di

import android.content.Context
import android.hardware.usb.UsbManager
import com.test.hyrocoptertestapp.R
import com.test.hyrocoptertestapp.data.*
import com.test.hyrocoptertestapp.data.providers.LogDataProvider
import com.test.hyrocoptertestapp.data.providers.UsbDataProvider
import com.test.hyrocoptertestapp.utils.DI_LOG_DATA_PROVIDER
import com.test.hyrocoptertestapp.utils.DI_USB_DATA_PROVIDER
import com.test.hyrocoptertestapp.data.providers.InputDataProvider
import com.test.hyrocoptertestapp.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
}

val dataModule = module {
    single<IMarkerDataManager> {MarkerDataManager()}
    single<IUsbDataManager> {UsbDataManager(androidContext().getSystemService(Context.USB_SERVICE) as UsbManager)}
    single<ILogDataManager> { LogDataManager(androidContext().resources.getStringArray(R.array.header)) }
}

val providerModule = module {
    single<InputDataProvider>(named(DI_USB_DATA_PROVIDER)) { UsbDataProvider(androidContext().getSystemService(Context.USB_SERVICE) as UsbManager, androidContext()) }
    single<InputDataProvider>(named(DI_LOG_DATA_PROVIDER)) { LogDataProvider() }
}

val modules = mutableListOf(viewModelModule, dataModule, providerModule)