package org.unyw.api

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.coroutines.suspendCoroutine


data class FileRequest(val path: String)
data class FileRequestWrite(val path: String, val text: String)


suspend fun fileRead (p : ApiParams){
    val fileRequest = gson.fromJson(p.call.receiveText(), FileRequest::class.java)
    val text = withContext(Dispatchers.IO) {
        File(p.ms.filesDir.absolutePath + "/rootfs" + fileRequest.path).readText()
    }
    p.call.respond(mapOf("status" to "success", "text" to text))

}

suspend fun fileWrite (p : ApiParams){
    val fileRequest = gson.fromJson(p.call.receiveText(), FileRequestWrite::class.java)
    val list = withContext(Dispatchers.IO) {
        File(p.ms.filesDir.absolutePath + "/rootfs" + fileRequest.path).writeText(fileRequest.text)//readText()
    }
    p.call.respond(mapOf("status" to "success"))
}

suspend fun fileList (p : ApiParams){
    val fileRequest = gson.fromJson(p.call.receiveText(), FileRequest::class.java)
    val list : MutableMap<String, Map<String, Any>> = mutableMapOf()

    withContext(Dispatchers.IO) {
        File(p.ms.filesDir.absolutePath + "/rootfs" + fileRequest.path)
            .list().filter { s -> !s.startsWith(".proot") }
            .forEach { s : String ->
                Log.d("cio", "ciao")
                val attr = Files.readAttributes(
                    Paths.get(p.ms.filesDir.absolutePath + "/rootfs" + fileRequest.path),
                    BasicFileAttributes::class.java)

                list[s] = mapOf(
                    "creationTime" to attr.creationTime().toString(),
                    "lastAccessTime" to attr.lastAccessTime().toString(),
                    "lastModifiedTime" to attr.lastModifiedTime().toString(),
                    "isDirectory" to attr.isDirectory,
                    "isFile" to attr.isRegularFile,
                    "isSymbolicLink" to attr.isSymbolicLink,
                    "size" to attr.size(),
                )
            }
    }
    p.call.respond(mapOf("status" to "success", "list" to list))
}