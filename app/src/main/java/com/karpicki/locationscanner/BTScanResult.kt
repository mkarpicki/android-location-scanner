package com.karpicki.locationscanner

import android.bluetooth.le.ScanResult
import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class BTScanResult(val scanResult: ScanResult, val timestamp: Long, val rssi: Int, val location: Location?):
    Parcelable {
//    private lateinit var _scanResult: ScanResult
//    private var timestamp : Long = 0
//    private var rssi: Long = 0

}