package com.example.systemvitalsdashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 101

    private lateinit var textViewWifiSsid: TextView
    private lateinit var textViewWifiRssi: TextView
    private lateinit var textViewWifiLinkSpeed: TextView
    private lateinit var textViewWifiFrequency: TextView
    private lateinit var textViewCellularType: TextView
    private lateinit var textViewCellularStrength: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeTextViews()

        if (checkPermissions()) {
            startMonitoring()
        } else {
            requestPermissions()
        }
    }

    private fun initializeTextViews() {
        textViewWifiSsid = findViewById(R.id.textViewWifiSsid)
        textViewWifiRssi = findViewById(R.id.textViewWifiRssi)
        textViewWifiLinkSpeed = findViewById(R.id.textViewWifiLinkSpeed)
        textViewWifiFrequency = findViewById(R.id.textViewWifiFrequency)
        textViewCellularType = findViewById(R.id.textViewCellularType)
        textViewCellularStrength = findViewById(R.id.textViewCellularStrength)
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startMonitoring()
            } else {
                Toast.makeText(this, "Permissions required to run app.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startMonitoring() {
        runnable = Runnable {
            updateWifiInfo()
            // TODO: Add call to updateCellularInfo() here later

            handler.postDelayed(runnable, 2000)
        }
        handler.post(runnable)
    }

    private fun updateWifiInfo() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo

        if (wifiManager.isWifiEnabled) {
            textViewWifiSsid.text = "SSID: ${wifiInfo.ssid}"
            val rssi = wifiInfo.rssi
            textViewWifiRssi.text = "Signal Strength (RSSI): $rssi dBm"
            textViewWifiLinkSpeed.text = "Link Speed: ${wifiInfo.linkSpeed} Mbps"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textViewWifiFrequency.text = "Frequency: ${wifiInfo.frequency} MHz"
            } else {
                textViewWifiFrequency.text = "Frequency: N/A on this Android version"
            }
        } else {
            textViewWifiSsid.text = "SSID: Wi-Fi is disabled"
            textViewWifiRssi.text = "Signal Strength (RSSI): -- dBm"
            textViewWifiLinkSpeed.text = "Link Speed: -- Mbps"
            textViewWifiFrequency.text = "Frequency: -- MHz"
        }
    }

    override fun onPause() {
        super.onPause()
        if(this::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            if (this::runnable.isInitialized) {
                handler.post(runnable)
            } else {
                startMonitoring()
            }
        }
    }
}
