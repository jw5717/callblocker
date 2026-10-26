package com.jw.callblocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jw.callblocker.data.BlockedCall
import com.jw.callblocker.data.CallStore

class CallScreeningServiceImpl : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        try {
            // Only screen incoming calls; let outgoing calls proceed untouched.
            if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
                allow(callDetails)
                return
            }

            val store = CallStore.get(this)

            // Master toggle off → let everything through.
            if (!store.isEnabled) {
                allow(callDetails)
                return
            }

            val number = callDetails.handle?.schemeSpecificPart
            val allowed = number != null &&
                (store.isWhitelisted(number) || isInContacts(number))

            if (allowed) {
                allow(callDetails)
            } else {
                store.logBlockedCall(number)
                if (store.isNotifyOnBlock) {
                    postBlockedNotification(number)
                }
                respondToCall(
                    callDetails,
                    CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipNotification(true)
                        .setSkipCallLog(true)
                        .build()
                )
            }
        } catch (e: Exception) {
            // Never leave a call unanswered — on any unexpected error, allow it through.
            Log.e(TAG, "onScreenCall failed; allowing call", e)
            allow(callDetails)
        }
    }

    private fun allow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
    }

    private fun isInContacts(number: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        return try {
            contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
                ?.use { it.moveToFirst() } ?: false
        } catch (e: SecurityException) {
            false
        }
    }

    private fun postBlockedNotification(number: String?) {
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Blocked calls",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Shown when a call is blocked." }
            )

            val display = if (number.isNullOrBlank()) BlockedCall.UNKNOWN_LABEL else number

            val tapIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Call blocked")
                .setContentText(display)
                .setAutoCancel(true)
                .setContentIntent(tapIntent)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .build()

            // Unique id per call so multiple blocks stack instead of overwriting.
            val id = (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
            NotificationManagerCompat.from(this).notify(id, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted (Android 13+). Blocking still worked.
            Log.w(TAG, "Could not post notification (permission not granted)")
        }
    }

    companion object {
        private const val TAG = "CallBlocker"
        private const val CHANNEL_ID = "blocked_calls"
    }
}
