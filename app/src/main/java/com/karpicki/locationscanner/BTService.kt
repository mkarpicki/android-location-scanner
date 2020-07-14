package com.karpicki.locationscanner

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.lang.reflect.Array.get

class BTService: Service() {

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var btDevices: ArrayList<BluetoothDevice>

//    private val bluetoothLeScanner: BluetoothLeScanner
//        get() {
//            val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//            val bluetoothAdapter = bluetoothManager.adapter
//            return bluetoothAdapter.bluetoothLeScanner
//        }

    private val bleScanner = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            //val str = "onScanResult: ${result?.device?.address} - ${result?.device?.name}"

            val resultList = ArrayList<ScanResult>()
            resultList.add(result as ScanResult)

            val scanIntent = Intent("bt_scan_update")
            scanIntent.putExtra("bt_results", resultList)
            sendBroadcast(scanIntent)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            //val str = "onBatchScanResults:${results.toString()}"
        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            TODO("warn no BT")
        }
        if (!bluetoothAdapter!!.isEnabled) {
            //val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            TODO("inform back that no BT enabled")
        }

        bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner

        bluetoothLeScanner.startScan(bleScanner)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothLeScanner.stopScan(bleScanner)
    }
}