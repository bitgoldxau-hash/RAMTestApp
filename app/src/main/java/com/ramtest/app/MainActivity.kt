package com.ramtest.app

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val MINIMUM_RAM_GB = 10

    // Must match EXACTLY with RAMBoosterPro's shared prefs
    private val BOOSTER_PACKAGE  = "com.rambooster.pro"
    private val PREFS_NAME       = "ram_booster_shared"
    private val KEY_BOOSTED_GB   = "boosted_ram_gb"
    private val KEY_IS_BOOSTED   = "is_boosted"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAndGate()
    }

    override fun onResume() {
        super.onResume()
        checkAndGate()
    }

    private fun getReportedRamGB(): Int {
        // First check if RAM Booster has boosted the RAM
        return try {
            val boosterPrefs = createPackageContext(
                BOOSTER_PACKAGE,
                Context.CONTEXT_IGNORE_SECURITY
            ).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val isBoosted = boosterPrefs.getBoolean(KEY_IS_BOOSTED, false)
            val boostedGB = boosterPrefs.getInt(KEY_BOOSTED_GB, 0)

            if (isBoosted && boostedGB >= MINIMUM_RAM_GB) {
                boostedGB // Return boosted value
            } else {
                getPhysicalRamGB() // Return real RAM
            }
        } catch (e: Exception) {
            // RAM Booster not installed or not boosted
            getPhysicalRamGB()
        }
    }

    private fun getPhysicalRamGB(): Int {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.totalMem / 1024 / 1024 / 1024).toInt()
    }

    private fun checkAndGate() {
        val physicalGB = getPhysicalRamGB()
        val reportedGB = getReportedRamGB()
        val isBoosted  = reportedGB > physicalGB

        val tvStatus   = findViewById<TextView>(R.id.tv_status)
        val tvRamInfo  = findViewById<TextView>(R.id.tv_ram_info)
        val tvRequired = findViewById<TextView>(R.id.tv_required)

        tvRequired.text = "Required: ${MINIMUM_RAM_GB} GB RAM"
        tvRamInfo.text  =
            "Physical RAM: ${physicalGB} GB\n" +
            "Boosted RAM:  ${if (isBoosted) "$reportedGB GB ✅" else "Not boosted"}\n" +
            "Reported to app: ${reportedGB} GB"

        if (reportedGB < MINIMUM_RAM_GB) {
            // ❌ BLOCKED
            tvStatus.text = "❌ BLOCKED"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_light))

            AlertDialog.Builder(this)
                .setTitle("🚫 RAM Insufficient")
                .setMessage(
                    "This app requires ${MINIMUM_RAM_GB} GB RAM.\n\n" +
                    "Your RAM: ${physicalGB} GB\n\n" +
                    "👉 Open RAM Booster Pro\n" +
                    "👉 Boost RAM to ${MINIMUM_RAM_GB} GB\n" +
                    "👉 Then come back here!\n\n" +
                    "App will now close."
                )
                .setPositiveButton("Open RAM Booster") { _, _ ->
                    // Try to open RAM Booster
                    try {
                        val intent = packageManager.getLaunchIntentForPackage(BOOSTER_PACKAGE)
                        if (intent != null) startActivity(intent)
                    } catch (e: Exception) { }
                    finishAffinity()
                }
                .setNegativeButton("Close") { _, _ -> finishAffinity() }
                .setCancelable(false)
                .show()

            // Auto close after 8 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                finishAffinity()
            }, 8000)

        } else {
            // ✅ ALLOWED
            tvStatus.text = "✅ ACCESS GRANTED"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_light))
        }
    }
}
