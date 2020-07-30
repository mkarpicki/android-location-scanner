package com.karpicki.locationscanner

import android.net.wifi.ScanResult
import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class WIFIScanResult(val scanResult: ScanResult, val timestamp: Long, val rssi: Int, val location: Location?):
    Parcelable {
}