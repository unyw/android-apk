package org.unyw.api

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.unyw.IntentResult
import java.lang.Exception
import androidx.core.content.FileProvider
import org.unyw.rndStr
import java.io.File


data class AuthenticateRequest(val title: String = "", val text: String = "")



suspend fun intentAuthenticate (p : ApiParams){
    val tx = p.call.receiveText()
    val authenticateRequest = gson.fromJson(tx, AuthenticateRequest::class.java)
    val activity = p.ms.activity
    if(activity != null) {
        val channel = Channel<Boolean>()

        var executor = ContextCompat.getMainExecutor(p.ms)
        var biometricPrompt = BiometricPrompt(
            activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    runBlocking {
                        channel.send(false)
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    runBlocking {
                        channel.send(true)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runBlocking {
                        channel.send(false)
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(authenticateRequest.title)
            .setSubtitle(authenticateRequest.text)
            // Can't call setNegativeButtonText() and
            // setAllowedAuthenticators(... or DEVICE_CREDENTIAL) at the same time.
            // .setNegativeButtonText("Use account password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        activity.runOnUiThread{
            biometricPrompt.authenticate(promptInfo)
        }


        if(channel.receive()){
            p.call.respond(mapOf("status" to "success", "authenticated" to true))
        }else{
            p.call.respond(mapOf("status" to "failure", "authenticated" to false))

        }
    }
}


data class PhotoRequest(val file: String?, val folder: String? ="pictures")

suspend fun intentPhoto(p:ApiParams){
    val photoRequest = gson.fromJson(p.call.receiveText(), PhotoRequest::class.java)

    val activity = p.ms.activity
    if(activity != null ) {
        val file = photoRequest.file ?: (rndStr(32) + ".jpg")
        val folder = photoRequest.folder ?: "pictures"
        val REQUEST_IMAGE_CAPTURE = p.ms.intentCounter++;
        val channel = Channel<IntentResult>()
        p.ms.intentChannels[REQUEST_IMAGE_CAPTURE] = channel
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoURI: Uri = FileProvider.getUriForFile(
            activity,
            "org.unyw.fileprovider",
            File(activity.getExternalFilesDir(folder), file)
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        val intentResult = channel.receive()
        if(intentResult.resultCode != Activity.RESULT_OK) return
        p.call.respond(mapOf("status" to "success", "type" to "image/jpeg", "file" to "/storage/unyw/$folder/$file"))

    }

}

data class FilepickerRequest(val mimetype: String? = "*/*", val file: String?, val folder: String? ="sent")

suspend fun intentFilepicker(p: ApiParams){
    val filepickerRequest = gson.fromJson(p.call.receiveText(), FilepickerRequest::class.java)

    val activity = p.ms.activity
    if(activity != null ) {
        val file = filepickerRequest.file ?: rndStr(32)
        val folder = filepickerRequest.folder ?: "sent"
        val REQUEST_FILEPICKER = p.ms.intentCounter++;
        val channel = Channel<IntentResult>()
        p.ms.intentChannels[REQUEST_FILEPICKER] = channel
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = filepickerRequest.mimetype ?: "*/*"
        }

        activity.startActivityForResult(intent, REQUEST_FILEPICKER)

        val intentResult = channel.receive()
        if(intentResult.resultCode != Activity.RESULT_OK) return
        Log.d("EHM", File(activity.getExternalFilesDir(folder), file).absolutePath ?: "")
        val fileUri: Uri = intentResult.data!!.data!!

        activity.contentResolver.openInputStream(fileUri)
            .copyTo(File(activity.getExternalFilesDir(folder), file).outputStream())

        p.call.respond(mapOf("status" to "success", "type" to intentResult.data?.type, "file" to "/storage/unyw/$folder/$file"))

    }
}

data class OpenRequest(val file: String = "", val folder: String? ="", val mimetype : String? = "*/*")

suspend fun intentOpen(p:ApiParams){
    val openRequest = gson.fromJson(p.call.receiveText(), OpenRequest::class.java)

    val activity = p.ms.activity
    if(activity != null ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = openRequest.mimetype ?: "*/*" // "*/*" will accepts all types of files, if you want specific then change it on your need.
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val fileURI = FileProvider.getUriForFile(
                p.ms, "org.unyw.fileprovider",
                File(activity.getExternalFilesDir(openRequest.folder ?: ""), openRequest.file)
            )
            putExtra(Intent.EXTRA_STREAM, fileURI)
        }
        activity.startActivity(shareIntent)
        p.call.respond(mapOf("status" to "success"))
    }
}
