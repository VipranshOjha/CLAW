package com.example.clawlite

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val TAG = "ClawLiteHID"

    private lateinit var webView: WebView
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothHidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null

    // For BluetoothProfile proxy
    private val HID_DEVICE_PROFILE = 19 // Available in newer SDKs, hardcoded for safety

    // HID Report Descriptor for a standard Gamepad
    // Report ID: 1
    // Byte 0-1: 14 buttons (16 bits total mapped to bytes 0-1)
    //   - Byte 0: A, B, X, Y, LB, RB, Back, Start
    //   - Byte 1: LS Click, RS Click, 4 Extras
    // Byte 2: D-Pad (Hat switch)
    // Byte 3-6: Left Stick X/Y, Right Stick X/Y
    private val HID_REPORT_DESCRIPTOR = byteArrayOf(
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x05.toByte(), // USAGE (Gamepad)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), 0x01.toByte(), //   REPORT_ID (1)
        // Buttons (Byte 0-1)
        0x05.toByte(), 0x09.toByte(), //   USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x0E.toByte(), //   USAGE_MAXIMUM (Button 14)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x0E.toByte(), //   REPORT_COUNT (14)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x02.toByte(), //   REPORT_COUNT (2) - Padding to 16 bits
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs)
        // D-Pad / Hat Switch (Byte 2)
        0x05.toByte(), 0x01.toByte(), //   USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x39.toByte(), //   USAGE (Hat switch)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x07.toByte(), //   LOGICAL_MAXIMUM (7)
        0x35.toByte(), 0x00.toByte(), //   PHYSICAL_MINIMUM (0)
        0x46.toByte(), 0x3B.toByte(), 0x01.toByte(), // PHYSICAL_MAXIMUM (315)
        0x65.toByte(), 0x14.toByte(), //   UNIT (Eng Rot:Angular Pos)
        0x75.toByte(), 0x04.toByte(), //   REPORT_SIZE (4)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x42.toByte(), //   INPUT (Data,Var,Abs,Null) (Null state = 8)
        0x75.toByte(), 0x04.toByte(), //   REPORT_SIZE (4)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1) - Padding to 8 bits
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs)
        // Joysticks (Bytes 3,4,5,6)
        0x05.toByte(), 0x01.toByte(), //   USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x01.toByte(), //   USAGE (Pointer)
        0xA1.toByte(), 0x00.toByte(), //   COLLECTION (Physical)
        0x09.toByte(), 0x30.toByte(), //     USAGE (X) - Left Stick X
        0x09.toByte(), 0x31.toByte(), //     USAGE (Y) - Left Stick Y
        0x09.toByte(), 0x32.toByte(), //     USAGE (Z) - Right Stick X
        0x09.toByte(), 0x35.toByte(), //     USAGE (Rz) - Right Stick Y
        0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7F.toByte(), //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
        0x95.toByte(), 0x04.toByte(), //     REPORT_COUNT (4)
        0x81.toByte(), 0x02.toByte(), //     INPUT (Data,Var,Abs)
        0xC0.toByte(),                //   END_COLLECTION
        0xC0.toByte()                 // END_COLLECTION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen WebView UI
        webView = WebView(this)
        setContentView(webView)
        
        setupWebView()
        checkPermissionsAndInitBluetooth()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        
        // Register the JS interface
        webView.addJavascriptInterface(GamepadBridge(), "ClawGamepad")
        
        // Load index.html from assets
        try {
            webView.loadUrl("file:///android_asset/index.html")
            
            /* Temporary debug UI removed */
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load WebView UI", e)
        }
    }

    private fun checkPermissionsAndInitBluetooth() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        val missing = permissions.filter { 
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED 
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        } else {
            initBluetoothHID()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initBluetoothHID()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required for HID.", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initBluetoothHID() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Please enable Bluetooth in settings", Toast.LENGTH_SHORT).show()
            return
        }

        // Connect to the HID_DEVICE profile
        bluetoothAdapter?.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == HID_DEVICE_PROFILE) {
                    bluetoothHidDevice = proxy as BluetoothHidDevice
                    registerApp()
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == HID_DEVICE_PROFILE) {
                    bluetoothHidDevice = null
                }
            }
        }, HID_DEVICE_PROFILE)
    }

    @SuppressLint("MissingPermission")
    private fun registerApp() {
        // 0x08 represents Peripheral Minor Class Gamepad
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "CLAW Lite Gamepad",
            "Zero-Driver Gamepad by CLAW",
            "CLAW",
            0x08.toByte(),
            HID_REPORT_DESCRIPTOR
        )

        val executor = Executors.newSingleThreadExecutor()

        bluetoothHidDevice?.registerApp(sdpSettings, null, null, executor, object : BluetoothHidDevice.Callback() {
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                super.onAppStatusChanged(pluggedDevice, registered)
                Log.d(TAG, "HID App Registered Status: $registered")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "HID Ready! Pair with PC.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                super.onConnectionStateChanged(device, state)
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    connectedDevice = device
                    Log.d(TAG, "HID Connected to: ${device?.name}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Connected to ${device?.name}", Toast.LENGTH_SHORT).show()
                    }
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    connectedDevice = null
                    Log.d(TAG, "HID Disconnected from: ${device?.name}")
                }
            }
        })
    }

    /**
     * JavaScript Bridge (The "ClawGamepad" object in JS)
     */
    inner class GamepadBridge {
        
        @SuppressLint("MissingPermission")
        @JavascriptInterface
        fun sendControllerState(b0: Int, b1: Int, dpad: Int, lx: Int, ly: Int, rx: Int, ry: Int) {
            // Byte 0: Buttons 1-8 (A, B, X, Y, LB, RB, Back, Start)
            // Byte 1: Buttons 9-14 (LS, RS, and 4 Extras)
            // Byte 2: D-Pad (0-7, 8 is neutral)
            val report = byteArrayOf(
                b0.toByte(), 
                b1.toByte(), 
                dpad.toByte(), 
                lx.toByte(), 
                ly.toByte(), 
                rx.toByte(), 
                ry.toByte()
            )
            
            if (bluetoothHidDevice == null || connectedDevice == null) {
                return
            }
            
            try {
                bluetoothHidDevice?.sendReport(connectedDevice, 1, report)
            } catch (e: Exception) {
                Log.e(TAG, "HID Report Error", e)
            }
        }
    }
}
