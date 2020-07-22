package com.karpicki.locationscanner

import android.net.wifi.ScanResult
import android.location.Location

class WIFIScanResult(val scanResult: ScanResult, val timestamp: Long, val rssi: Int, val location: Location?) {
}