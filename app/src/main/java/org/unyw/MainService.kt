package org.unyw

import android.content.Intent
import android.os.IBinder
import android.app.*
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Binder

import androidx.core.app.NotificationCompat

import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import org.unyw.api.startApi
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import android.graphics.Bitmap

import android.content.res.AssetManager
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.io.InputStream


var isServiceRunning = false
var mainActivity : MainActivity? = null

class IntentResult (public var resultCode: Int, public var data: Intent?)

class MainService : Service() {

    lateinit var CHANNEL_ID : String
    var debug = false
    lateinit var tokens : Map<String, String>
    lateinit var vncScreenSize : Point

    var activity : MainActivity? = null
        get() = mainActivity

    var intentCounter = 1
    val intentChannels = mutableMapOf<Int, Channel<IntentResult>>()


    override fun onCreate() {
        super.onCreate()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        debug = File(filesDir.absolutePath + "/rootfs/var/unyw/settings/DEBUG_MODE").exists()
        tokens = mapOf(
            "UNYW_TOKEN_API" to if(debug) "debugapi" else rndStr(16),
            "UNYW_TOKEN_SSH" to if(debug) "debugssh" else rndStr(16),
            "UNYW_TOKEN_VNC" to if(debug) "debugvnc" else rndStr(16),
        )
        tokens.forEach { (key, value) -> Log.d(key, value)}
        apiToken = tokens["UNYW_TOKEN_API"]

        vncScreenSize = Point().also {
            val realScreenSize = Point()
            (application.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(realScreenSize)
            //val maxDim = Math.max(realScreenSize.x.dp, realScreenSize.y.dp)
            //it.set(maxDim, maxDim)
            it.set(realScreenSize.x.dp, realScreenSize.y.dp)
        }

        isServiceRunning = true
        sshLock.acquire(MAX_SSH_CONNECTIONS)
        thread { startProot(this@MainService)  }
        thread { startSSH(this@MainService)    }
        thread { startApi(this@MainService)    }
    }





    // HANDLE ANDROID THINGS (NOTIFICATION, BINDING TO ACTIVITY, ...)

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getApiToken() = tokens["UNYW_TOKEN_API"]
        // Return this instance of LocalService so clients can call public methods
        fun setMainActivity( a: MainActivity? ){
            mainActivity = a
        }

        fun setIntentResult(requestCode: Int, intentResult: IntentResult){
            runBlocking {
                intentChannels[requestCode]?.send(intentResult)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(intent != null && intent.hasExtra("STOP_SERVICE")){
            stopForeground(0)
            stopSelf()
            return START_NOT_STICKY
        }

        CHANNEL_ID =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("unyw_service", "Unyw Background Service")
            } else { ""  }

        val notificationIntent = Intent(this, MainService::class.java).putExtra("STOP_SERVICE", "stop")
        val pendingIntent = PendingIntent.getService(
            this,
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Unyw is running")
            .setContentText("Click here to stop")
            //.setColorized(true)
            .setColor(resources.getColor(R.color.blue_main))
            .setSmallIcon(R.drawable.ic_penguin) // TODO: change to Unyw icon
            .setContentIntent(pendingIntent)
            .build()


        startForeground(124, notification)
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onBind(p0: Intent?): IBinder? = binder
    override fun onUnbind(intent: Intent?): Boolean {
        mainActivity = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        thread {
            Thread.sleep(2_000)
            exitProcess(0)
        }
        mainActivity?.finish()
        exitProcess(0)
        super.onDestroy()
    }
}