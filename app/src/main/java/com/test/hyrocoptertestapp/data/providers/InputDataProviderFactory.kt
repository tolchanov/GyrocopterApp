package com.test.hyrocoptertestapp.data.providers

import com.test.hyrocoptertestapp.utils.DI_LOG_DATA_PROVIDER
import com.test.hyrocoptertestapp.utils.DI_USB_DATA_PROVIDER
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

object InputDataProviderFactory : KoinComponent {
    val usbDataProvider by inject<InputDataProvider>(named(DI_USB_DATA_PROVIDER))
    val logDataProvider by inject<InputDataProvider>(named(DI_LOG_DATA_PROVIDER))

    fun getInputDataProvider(isUsb: Boolean): InputDataProvider {
        return if (isUsb) {
            usbDataProvider
        } else {
            logDataProvider
        }
    }
}