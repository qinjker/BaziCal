package com.bazical.app.utils

import java.util.UUID

object DeviceId {
    private var deviceId: String? = null

    fun getDeviceId(): String {
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
        }
        return deviceId!!
    }
}