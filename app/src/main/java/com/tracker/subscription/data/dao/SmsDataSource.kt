package com.tracker.subscription.data.dao

import android.content.Context
import android.net.Uri
import com.tracker.subscription.data.Sms

class SmsDataSource(private val context: Context) {

    fun readSms(): List<Sms> {
        val messages = mutableListOf<Sms>()

        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            null,
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex) // 👈 THIS IS IMPORTANT

                messages.add(Sms(body = body, date = date))
            }
        }

        return messages
    }
}