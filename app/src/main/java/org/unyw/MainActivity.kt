package org.unyw

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*

import android.view.WindowManager
import android.webkit.WebResourceResponse
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.unyw.api.gson
import java.io.File
import java.net.URI
import java.util.*
import kotlin.concurrent.thread

var apiToken : String? = null

class MainActivity : AppCompatActivity() {

    val WEBSITE = "http://localhost:12079"
    var webView: WebView? = null
    var lastUrl : String? = null

    private var binder : MainService.LocalBinder? = null
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("UNYW_TOKEND", "service connected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            binder = service as MainService.LocalBinder
            apiToken = binder?.getApiToken()

            binder?.setMainActivity(this@MainActivity)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            binder = null
        }
    }

    fun handleIntent(intent: Intent?) : String? {
        if(intent == null) return null
        if(intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_VIEW){
            val map = mutableMapOf<String,String>()
            map["type"] = intent.type
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                map["text"] = it
            }
            if(intent.data?.path != null){
                var path = intent.data.path.toString()
                if(path.startsWith("/file/sdcard")) path = "/storage/external" + path.substring("/file/sdcard".length)
                Log.d("UNYW_PATH",path)
                map["file"] = path

            }else{
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    val file = rndStr(32)
                    contentResolver.openInputStream(it).copyTo(File(getExternalFilesDir("sent"), file).outputStream())
                    map["file"] = "/storage/unyw/sent/$file"
                }
                (intent?.dataString as? Uri)?.let{
                    val file = rndStr(32)
                    contentResolver.openInputStream(it)
                        .copyTo(File(getExternalFilesDir("sent"), file).outputStream())
                    map["file"] = "/storage/unyw/sent/$file"
                }
            }
            return "$WEBSITE/apps/home/index.html#unyw=${Base64.getEncoder().encodeToString(gson.toJson(map).toByteArray())}"

        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun setupWebview(){
            setContentView(R.layout.activity_main)
            webView = findViewById(R.id.mainWebView)
            webView!!.settings.setJavaScriptEnabled(true)
            WebView.setWebContentsDebuggingEnabled(true)

            webView!!.webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Log.d("UNYW_WEBVIEW_LASTURL", url)
                    lastUrl = url
                    super.onPageStarted(view, url, favicon)
                }
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    if(request == null || request.url == null) return super.shouldInterceptRequest(view, request)
                    val url = URI(
                            request.url.scheme,
                            request.url.authority,
                            request.url.path,
                            null,  // Ignore the query part of the input url
                            null,
                        ).toString()

                    val lUrl = lastUrl
                    if(url != null && lUrl != null){
                        val lastURI = URI(lUrl)
                        val lastUrlBase = URI(
                            lastURI.scheme,
                            lastURI.authority,
                            lastURI.path,
                            null,  // Ignore the query part of the input url
                            null
                        ).toString()

                        if(url == lastUrlBase){
                            webView!!.post{
                                webView!!.evaluateJavascript("window.location.replace(${jsStr(lUrl)}",null)
                            }
                        }
                    }


                    if (url != null && url.startsWith(WEBSITE)) {

                        if (url == "$WEBSITE/UNYW_TOKEN_API.json") return WebResourceResponse(
                            "application/json",
                            "utf-8",
                            """{"token":"$apiToken"}""".byteInputStream())

                        if(url.startsWith("$WEBSITE/internal")){
                            if (url == "$WEBSITE/internal/EMPTY.html") return WebResourceResponse(
                                "text/html",
                                "utf-8",
                                PageEmpty.byteInputStream())

                            if (url == "$WEBSITE/internal/SPLASHSCREEN.html") return WebResourceResponse(
                                "text/html",
                                "utf-8",
                                PageSplashscreen.byteInputStream())

                            if (url == "$WEBSITE/internal/INSTALL.html") return WebResourceResponse(
                                "text/html",
                                "utf-8",
                                PageInstall.byteInputStream())
                        }
                        return try {
                            WebResourceResponse(
                                mimeTypes[url.substring(url.lastIndexOf('.') + 1)],
                                "utf-8",
                                File(
                                    url.replace(
                                        "$WEBSITE/",
                                        filesDir.absolutePath + "/rootfs/usr/share/unyw/"
                                    )
                                ).inputStream()
                            )
                        } catch (e: Exception) {
                            WebResourceResponse("text/html","utf-8", Page404(url.replace("$WEBSITE", "")).byteInputStream())
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }
        }

        fun startMainService() =  startService(Intent(this@MainActivity, MainService::class.java ))


        if(isInstalled(this@MainActivity)) {
            val wasRunning = isServiceRunning
            startMainService()
            setupWebview()
            var url = handleIntent(intent) ?: "$WEBSITE/apps/home/index.html"
            if(intent.hasExtra("url")) {
                url = intent.extras["url"].toString()
                if (url.startsWith("/")) url = "$WEBSITE$url"
            }


            if(!wasRunning)  url = "$WEBSITE/internal/SPLASHSCREEN.html#$url"

            webView?.loadUrl(url)
            return
        }

        setupWebview()
        if(isServiceRunning){
            webView?.loadUrl("$WEBSITE/internal/INSTALL.html")
            return
        }

        webView?.loadUrl("$WEBSITE/internal/EMPTY.html")
        mainActivity = this@MainActivity
        AlertDialog.Builder(this).create().apply {
            setTitle("Unyw is still a demo")
            setMessage("The app has major security issues, is not suitable for distribution")
            setCancelable(false)
            setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ -> finish() }
            setButton(DialogInterface.BUTTON_POSITIVE, "Confirm") { _, _ ->
                AlertDialog.Builder(this@MainActivity).create().apply {
                    setTitle("Warning")
                    setMessage("This will download ~15mb. Proceed?")
                    setCancelable(false)
                    setButton(DialogInterface.BUTTON_POSITIVE, "Ok") { _, _ ->
                        // On android Marshmallow and after
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this@MainActivity,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    1 // FILE_PERMISSION_REQUEST_CODE
                                )
                            }
                        }
                        webView?.evaluateJavascript("window.location.replace('$WEBSITE/internal/INSTALL.html')") {}
                        startMainService()
                    }
                    setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ -> finish()}
                }.show()
            }
        }.show()
    }

    override fun onNewIntent(intent: Intent?) {
        val intentUrl = handleIntent(intent)
        if(intentUrl != null) {
            thread {
                if(!isServiceRunning) Thread.sleep(1000)
                webView?.post { webView?.loadUrl(intentUrl)}
            }
        }else if(intent?.hasExtra("url") == true){
            thread {
                var url = intent.extras["url"].toString()
                if(url.startsWith("/")) url = "$WEBSITE$url"
                if(!isServiceRunning) Thread.sleep(1000)
                webView?.post { webView?.loadUrl(url)}
            }
        }
        super.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binder?.setIntentResult(requestCode, IntentResult(resultCode, data))
    }


    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.d("UNYW_KEYD", event?.toString() ?: "null")
        return super.dispatchKeyEvent(event)
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(webView!!.canGoBack()){
                webView!!.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onStart() {
        super.onStart()
        Intent(this, MainService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_ABOVE_CLIENT)
        }
    }

    override fun onStop() {
        super.onStop()
        mainActivity = null
        binder?.setMainActivity(null)
        unbindService(connection)
    }


    /** Destroy activity, webview and force process exiting */
    override fun onDestroy() {
        webView!!.apply {              // Completely destroy webview (see https://stackoverflow.com/a/17458577)
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            onPause()
            removeAllViews()
            destroyDrawingCache()
            destroy()
        }
        super.onDestroy()                // Required by android, but never called
    }
}