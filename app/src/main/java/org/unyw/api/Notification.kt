package org.unyw.api

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.os.Looper
import android.os.VibrationEffect

import android.os.Build

import androidx.core.content.ContextCompat.getSystemService

import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.unyw.MainActivity
import org.unyw.MainService
import org.unyw.R


data class ToastRequest(val text: String, val long : Boolean = false)


suspend fun notificationToast (p : ApiParams){
    val toastRequest = gson.fromJson(p.call.receiveText(), ToastRequest::class.java)
    val duration = if(toastRequest.long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Handler(Looper.getMainLooper()).post(Runnable {
        Toast.makeText(p.ms, toastRequest.text, duration).show()
    })
    p.call.respond(mapOf("status" to "success"))

}

data class VibrateRequest(val millis: Long = 500)

suspend fun notificationVibrate (p : ApiParams){
    val vibrateRequest = gson.fromJson(p.call.receiveText(), VibrateRequest::class.java)
    val millis = vibrateRequest?.millis ?: 500
    val v = p.ms.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v!!.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        //deprecated in API 26
        v!!.vibrate(millis)
    }

    p.call.respond(mapOf("status" to "success"))

}

data class NotificationRequest(val url: String, val text: String, val title: String)

suspend fun notificationShow (p : ApiParams){
    val notificationRequest = gson.fromJson(p.call.receiveText(), NotificationRequest::class.java)
    val notificationIntent = Intent(p.ms, MainActivity::class.java).putExtra("url", notificationRequest.url)
    val pendingIntent = PendingIntent.getActivity(
        p.ms,
        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification: Notification = NotificationCompat.Builder(p.ms, p.ms.CHANNEL_ID)
        .setContentTitle(notificationRequest.title)
        .setContentText(notificationRequest.text)
        //.setColorized(true)
        .setColor(p.ms.resources.getColor(R.color.blue_main))
        .setSmallIcon(R.drawable.ic_penguin) // TODO: change to Unyw icon
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()


    with(NotificationManagerCompat.from(p.ms)) {
        // notificationId is a unique int for each notification that you must define
        notify(1, notification)
    }

    p.call.respond(mapOf("status" to "success"))

}

