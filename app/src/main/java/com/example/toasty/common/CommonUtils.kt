package com.example.toasty.common

import android.annotation.SuppressLint
import android.os.Build
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
    }
}