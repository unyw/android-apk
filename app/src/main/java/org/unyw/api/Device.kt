package org.unyw.api

import android.content.Context
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.hardware.camera2.CameraAccessException

import androidx.core.content.ContextCompat.getSystemService

import android.hardware.camera2.CameraManager
import androidx.core.content.ContextCompat
import android.os.BatteryManager

import android.content.Context.BATTERY_SERVICE
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import org.unyw.mainActivity


data class TorchRequest(val on: Boolean)


suspend fun deviceTorch (p : ApiParams){
    val torchRequest = gson.fromJson(p.call.receiveText(), TorchRequest::class.java)
    val camManager = p.ms.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = camManager.cameraIdList[0]
    camManager.setTorchMode(cameraId, torchRequest.on) //Turn ON
    p.call.respond(mapOf("status" to "success"))

}

suspend fun deviceBattery (p : ApiParams){
    val batManager = p.ms.getSystemService(BATTERY_SERVICE) as BatteryManager
    val level = batManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    p.call.respond(mapOf("status" to "success", "level" to level, "charging" to batManager.isCharging))
}