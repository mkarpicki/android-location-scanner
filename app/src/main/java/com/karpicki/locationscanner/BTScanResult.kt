package com.karpicki.locationscanner

import android.bluetooth.le.ScanResult
import android.location.Location

class BTScanResult(val scanResult: ScanResult, val timestamp: Long, val rssi: Int, val location: Location?) {
//    private lateinit var _scanResult: ScanResult
//    private var timestamp : Long = 0
//    private var rssi: Long = 0

}