package org.unyw

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest









class InstallerActivity : AppCompatActivity() {

    private val mirror = "https://unyw.github.io/android-rootfs/dist"

    // Preferences (used to store installation state)
    private lateinit var prefs : SharedPreferences

    private var areAssetsInstalled
        get() = prefs.getBoolean("org.unyw.install.assets", false)
        set(value) = prefs.edit().putBoolean("org.unyw.install.assets", value).apply()

    private var isDebianInstalled
        get() = prefs.getBoolean("org.unyw.install.debian", false)
        set(value) = prefs.edit().putBoolean("org.unyw.install.debian", value).apply()

    private var md5sum
        get() = prefs.getString("org.unyw.install.md5sum", "")
        set(value) = prefs.edit().putString("org.unyw.install.md5sum", value).apply()

    private fun refreshMd5sum(){

        lifecycleScope.launch {
            try {
                val document = withContext(Dispatchers.IO) {
                    URL("$mirror/md5.txt").readText()
                }
                Log.d("URL", document)
            }catch (e : Exception){
                Log.d("URL", "Md5 unreachable")
            }

        }
    }

    private fun startMainActivity(){
        startActivity(Intent(this@InstallerActivity, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        this.overridePendingTransition(0, 0) // Disable smooth animation
        finish() // Finish this activity
    }



    private lateinit var installButton : Button
    private fun setButtonOnDownloading(){
        installButton.isActivated = false
        installButton.isEnabled = false
        installButton.text = "    Downloading...    "
    }

    val rootfsUrl = "$mirror/debian_$systemArch.tar.xz"
    val rootfsFile = Environment.getExternalStorageDirectory().resolve(".unyw-temp/debian_$systemArch.tar.xz")
    val rootfsResult = Environment.getExternalStorageDirectory().resolve(".unyw-temp/debian.tar.xz")
    private lateinit var downloadManager : DownloadManager
    private fun startDownload(){
        if(rootfsFile.exists()) rootfsFile.delete() // Delete previous downloads

        Runtime.getRuntime().exec("/data/data/org.unyw/files/busybox touch /data/data/org.unyw/files/ecx2")

        return
        // Start download
        downloadManager.enqueue(DownloadManager.Request(Uri.parse(rootfsUrl)).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle("Unyw")
            setDescription("Downloading Debian base filesystem...")
            setVisibleInDownloadsUi(false)
            setDestinationUri(Uri.fromFile(rootfsFile))
        })

        this@InstallerActivity.setButtonOnDownloading()
    }

    fun endDownload(){
        try {
            if (rootfsFile.exists() && checkMD5(md5sum, rootfsFile)) {
                // Remember debian is installed
                isDebianInstalled = true
                rootfsFile.renameTo(rootfsResult)
                val query = DownloadManager.Query()
                query.setFilterByStatus(DownloadManager.STATUS_FAILED or DownloadManager.STATUS_PAUSED or DownloadManager.STATUS_PENDING or DownloadManager.STATUS_RUNNING)
                val c = downloadManager.query(query)
                while (c.moveToNext()) {
                    downloadManager.remove(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)))
                }
                startMainActivity()
            }
        }catch (e: Exception){}
    }

    // OnDownloadEnded
    val onDownloadEnded = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            endDownload()
        }
    }

    @SuppressLint("Range")
    private fun isDownloadRunning() : Boolean {
        try {
            val c = downloadManager.query(DownloadManager.Query().setFilterByStatus(
                DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING or DownloadManager.STATUS_PAUSED
            ))
            if (c != null && c.moveToFirst()) {
                do {
                    try {
                        if (c.getString(c?.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))?.endsWith("debian_$systemArch.tar.xz") == true)
                            return true
                    }catch(ignored: Error){}
                } while (c.moveToNext())
            }
        }catch (ignored : Error){}
        return false
    }

    private val FILE_PERMISSION_REQUEST_CODE = 1
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FILE_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startDownload()
                }
                return
            }
        }
    }


    // On create
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getPreferences(Context.MODE_PRIVATE)
        if(isDebianInstalled){
            startMainActivity()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installer)

        if(!areAssetsInstalled){
            val internalFolder = baseContext.filesDir
            // Copy asset file to internal dir
            fun copyFile(src : String, out: String) : File {
                val file = File(internalFolder, out)
                val fileOutputStream = FileOutputStream(file)
                assets.open(src).copyTo(fileOutputStream)
                fileOutputStream.close()
                file.setExecutable(true)
                return file
            }
            copyFile("busybox/$systemArch/busybox", "busybox")
            areAssetsInstalled = true
        }


        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        registerReceiver( onDownloadEnded, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Add on-click behaviour
        installButton = findViewById(R.id.installButton)
        installButton.setOnClickListener {
            AlertDialog.Builder(this).create().apply {
                setTitle("Unyw is still a demo")
                setMessage("The app has major security issues, is not suitable for distribution")
                setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ -> }
                setButton(DialogInterface.BUTTON_POSITIVE, "Confirm") { dialog, whichButton ->
                    AlertDialog.Builder(this@InstallerActivity).create().apply {
                        setTitle("Warning")
                        setMessage("This will download ~15mb. Proceed?")
                        setButton(DialogInterface.BUTTON_POSITIVE, "Ok") { dialog, whichButton ->
                            // On android Marshmallow and after
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (ContextCompat.checkSelfPermission(
                                        this@InstallerActivity,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    ActivityCompat.requestPermissions(
                                        this@InstallerActivity,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                        FILE_PERMISSION_REQUEST_CODE
                                    )
                                } else {
                                    startDownload()
                                }
                            } else {
                                startDownload()
                            }
                        }
                        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ -> }
                    }.show()
                }
            }.show()
        }
    }

    // On resume
    override fun onResume() {
        super.onResume()
        refreshMd5sum()
        if(isDownloadRunning()) setButtonOnDownloading()  // Check if download is still running
        endDownload()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadEnded)
    }

}