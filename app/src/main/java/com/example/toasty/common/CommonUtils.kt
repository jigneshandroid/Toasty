package com.example.toasty.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CommonUtils {

    companion object {
        private const val TAG = "CommonUtils"

        fun dateFormetter(
            date: Date,
            format: String,
            locale: Locale = Locale.getDefault()
        ): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(date)
        }

        fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }

        @SuppressLint("SimpleDateFormat")
        fun convertTimestampToDate(unixTimestamp: Long): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val netDate = Date(unixTimestamp)
                return sdf.format(netDate)
            } catch (e: Exception) {
                Log.d(TAG, "Exception:  ${e.toString()}")
                ""
            }
        }

        @SuppressLint("HardwareIds")
        fun getDeviceInfo(context: Context): String {
            val deviceInfo = StringBuilder()

            // Device Model
            deviceInfo.append("Model: ${Build.MODEL}\n")

            // Manufacturer
            deviceInfo.append("Manufacturer: ${Build.MANUFACTURER}\n")

            // OS Version
            deviceInfo.append("OS Version: ${Build.VERSION.RELEASE}\n")

            // SDK Version
            deviceInfo.append("SDK Version: ${Build.VERSION.SDK_INT}\n")

            // Device ID (Secure Android ID)
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            deviceInfo.append("Android ID: $androidId\n")

            // Build Number
            deviceInfo.append("Build Number: ${Build.DISPLAY}\n")

            // Device Brand
            deviceInfo.append("Brand: ${Build.BRAND}\n")

            // Product Name (e.g., Nexus 6P)
            deviceInfo.append("Product Name: ${Build.PRODUCT}\n")

            // Device Hardware
            deviceInfo.append("Hardware: ${Build.HARDWARE}\n")

            // Device Serial Number (may require permission for some devices)
          /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceInfo.append("Serial Number: ${Build.getSerial()}\n")
            }*/

            return deviceInfo.toString()
        }
    }
}