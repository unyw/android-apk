package org.unyw

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.util.*
import kotlin.concurrent.thread


val rootfs_url =  "https://unyw.github.io/android-rootfs/dist/rootfs_$systemArch.tar.xz"

fun isInstalled(context : Context) : Boolean = File(context.filesDir.toString() + "/unyw_installed.txt").exists();

fun installProot(ms: MainService){
    // Log in mainActivity's WebView
    fun log (str: String, callback : String = "Log"){
        Log.d("UNYW_INSTALL", str)

        if(mainActivity?.lastUrl == "${mainActivity?.WEBSITE}/internal/INSTALL.html") mainActivity?.webView?.post {
            // Post raw data about installation progress
            mainActivity!!.webView!!.evaluateJavascript("window.install$callback(`${str}`)") {}
        }
    }

    while(true) {
        try {
            Thread.sleep(1_000)
            log("Starting installation...", "Title")
            val internalFolder = ms.filesDir

            // Copy asset file to internal dir
            fun copyFile(src: String, out: String): File {
                log("Copying file '$out'")
                val file = File(internalFolder, out)
                val fileOutputStream = FileOutputStream(file)
                ms.assets.open(src).copyTo(fileOutputStream)
                fileOutputStream.close()
                file.setExecutable(true)
                return file
            }
            copyFile("binaries/$systemArch/busybox", "busybox")
            copyFile("binaries/$systemArch/proot", "proot")
            copyFile("binaries/$systemArch/libtalloc.so.2", "libtalloc.so.2")

            copyFile("install.sh", "install.sh")
            copyFile("proot.sh", "proot.sh")
            copyFile("start.sh", "start.sh")


            // Donwload rootfs
            log("Downloading rootfs from  $rootfs_url", "Title")
            FileOutputStream(ms.filesDir.absolutePath + "/rootfs.tar.xz").getChannel()
                .transferFrom(Channels.newChannel(URL(rootfs_url).openStream()), 0, Long.MAX_VALUE)
            log("Download done!")


            log("Running install.sh script", "Title")
            val p = ProcessBuilder("./busybox", "sh", "install.sh").apply {
                directory(File(ms.filesDir.absolutePath))
                environment().putAll(
                    mapOf(
                        "MACHINE_ID" to rndStr(32, ('0'..'9') + ('a'..'f')),
                        "INTERNAL_STORAGE" to ms.filesDir.absolutePath,
                        "EXTERNAL_STORAGE" to Environment.getExternalStorageDirectory().absolutePath,
                        "UNYW_ROOTFS_URL" to rootfs_url,
                        "SCREEN_WIDTH" to ms.vncScreenSize.x.toString(),
                        "SCREEN_HEIGHT" to ms.vncScreenSize.y.toString(),

                        )
                )
                environment().putAll(ms.tokens)
            }.start()
            thread { Scanner(p.inputStream).apply { while (hasNextLine()) log(nextLine()) } }
            thread {
                Scanner(p.errorStream).apply {
                    while (hasNextLine()) log(
                        nextLine(),
                        "Error"
                    )
                }
            }

            p.waitFor();
            if (isInstalled(ms)) {
                log("Process ended", "Title")
                return
            }
            log("Something went wrong! (incomplete installation)", "Error")
        } catch (e: Exception) {
            log(e.toString(), "Error")
            log("Retrying installation...", "Title")
        }
    }
}


fun startProot(ms: MainService){
    if(!isInstalled(ms)){
        installProot(ms)
        thread {
            Thread.sleep(1000)
            mainActivity?.webView?.post {
                mainActivity?.webView?.loadUrl("${mainActivity?.WEBSITE}/apps/home/index.html")
            }
        }
    }

    sshLock.release(MAX_SSH_CONNECTIONS) // Tell ssh server that it could start!

    while(true){
        try {
            Log.d("UNYW_SIZE", ms.vncScreenSize.x.toString())
            val internalFolder = ms.filesDir

            // Copy asset file to internal dir
            fun copyFile(src: String, out: String): File {
                val file = File(internalFolder, out)
                val fileOutputStream = FileOutputStream(file)
                ms.assets.open(src).copyTo(fileOutputStream)
                fileOutputStream.close()
                file.setExecutable(true)
                return file
            }
            copyFile("proot.sh", "proot.sh")
            copyFile("start.sh", "start.sh")

            val p = ProcessBuilder("./busybox", "sh", "start.sh").apply {
                directory(File(ms.filesDir.absolutePath))
                environment().putAll(
                    mapOf(
                        "UNYW_STORAGE" to ms.getExternalFilesDir(null).absolutePath,
                        "INTERNAL_STORAGE" to ms.filesDir.absolutePath,
                        "EXTERNAL_STORAGE" to Environment.getExternalStorageDirectory().absolutePath,
                        "UNYW_ROOTFS_URL" to rootfs_url,
                        "SCREEN_WIDTH" to ms.vncScreenSize.x.toString(),
                        "SCREEN_HEIGHT" to ms.vncScreenSize.y.toString(),

                    )
                )
                environment().putAll(ms.tokens)
            }.start()
            thread { Scanner(p.inputStream).apply { while (hasNextLine()) Log.d("UNYW_START", nextLine()) } }
            thread { Scanner(p.errorStream).apply { while (hasNextLine()) Log.e("UNYW_START", nextLine()) } }

            p.waitFor();
        }catch(e: Exception) {
            Log.e("UNYW_START", e.toString())
        }
        Thread.sleep(3_000)
    }
}