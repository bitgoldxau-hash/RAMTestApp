package com.ramtest.app

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // ⚠️ MINIMUM RAM REQUIRED = 10 GB
    private val MINIMUM_RAM_GB = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRamGB = (memInfo.totalMem / 1024 / 1024 / 1024).toInt()
        val totalRamMB = (memInfo.totalMem / 1024 / 1024).toInt()
        val freeRamMB  = (memInfo.availMem  / 1024 / 1024).toInt()

        val tvStatus   = findViewById<TextView>(R.id.tv_status)
        val tvRamInfo  = findViewById<TextView>(R.id.tv_ram_info)
        val tvRequired = findViewById<TextView>(R.id.tv_required)

        tvRequired.text = "Required RAM: ${MINIMUM_RAM_GB} GB minimum"
        tvRamInfo.text  = "Your Device RAM: ${totalRamGB} GB (${totalRamMB} MB total)\n" +
                          "Free RAM: ${freeRamMB} MB"

        if (totalRamGB < MINIMUM_RAM_GB) {
            // ❌ NOT ENOUGH RAM
            tvStatus.text = "❌ INSUFFICIENT RAM"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_light))

            AlertDialog.Builder(this)
                .setTitle("⚠️ Insufficient RAM")
                .setMessage(
                    "This app requires at least ${MINIMUM_RAM_GB} GB of RAM.\n\n" +
                    "Your device has only ${totalRamGB} GB of RAM.\n\n" +
                    "Please upgrade to a device with ${MINIMUM_RAM_GB} GB or more RAM to use this app."
                )
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()

        } else {
            // ✅ ENOUGH RAM
            tvStatus.text = "✅ RAM CHECK PASSED"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_light))
        }
    }
}
