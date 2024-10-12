package com.example.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListView: ListView
    private lateinit var wifiReceiver: BroadcastReceiver

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo các thành phần UI
        val scanWifiButton: Button = findViewById(R.id.scanWifiButton)
        wifiListView = findViewById(R.id.wifiListView)

        // Khởi tạo WifiManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Kiểm tra quyền truy cập vị trí
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }

        // Thiết lập sự kiện khi nhấn nút "Scan WiFi Networks"
        scanWifiButton.setOnClickListener {
            if (wifiManager.isWifiEnabled) {
                scanWifiNetworks()
            } else {
                Toast.makeText(this, "WiFi is disabled. Enabling WiFi...", Toast.LENGTH_SHORT).show()
                wifiManager.isWifiEnabled = true
            }
        }
    }

    // Hàm quét các mạng Wi-Fi
    private fun scanWifiNetworks() {
        wifiReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
                if (success) {
                    displayWifiNetworks()
                } else {
                    Toast.makeText(this@MainActivity, "Scan failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
    }

    // Hàm hiển thị danh sách các mạng Wi-Fi tìm được
    @SuppressLint("MissingPermission")
    private fun displayWifiNetworks() {
        val results: List<ScanResult> = wifiManager.scanResults
        val wifiList = mutableListOf<String>()
        for (result in results) {
            wifiList.add("SSID: ${result.SSID}, Signal Strength: ${result.level} dBm")
        }

        // Hiển thị danh sách Wi-Fi trong ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, wifiList)
        wifiListView.adapter = adapter
        unregisterReceiver(wifiReceiver)
    }

    // Xử lý kết quả yêu cầu quyền
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}