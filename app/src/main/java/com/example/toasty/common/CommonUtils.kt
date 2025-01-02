package com.example.toasty.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CommonUtils {

    companion object {

        fun dateFormetter(date:Date, format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(date)
        }

        fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }
    }
}