package org.unyw.api

import android.util.Log
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.unyw.bashStr
import org.unyw.ssh
import java.io.File


data class ProcessRequestRun(val command: String)

suspend fun processRun (p : ApiParams){

    val processRequest = gson.fromJson(p.call.receiveText(), ProcessRequestRun::class.java)
    p.call.respond(mapOf("status" to "success", "result" to ssh?.runCommand(processRequest.command)))
}


data class ProcessRequestDtach(val command: String, val socket: String)

suspend fun processDtach (p : ApiParams){
    val processRequest = gson.fromJson(p.call.receiveText(), ProcessRequestDtach::class.java)
    p.call.respond(mapOf("status" to "success", "result" to ssh?.runCommand(
        "dtach -n '/run/unyw/dtach/${processRequest.socket}' sh -c ${bashStr("echo $$ > /run/unyw/dtach-pids/${processRequest.socket};" + processRequest.command)}")
    ))

}


/*
 `DISPLAY=:3200 bspc monitor -a xterm1 && DISPLAY=:3200 bspc desktop xterm1 -f `
      + `&& DISPLAY=:3200 bspc config right_padding ${info.vnc.width - vncIframe.clientWidth} `
      + `&& DISPLAY=:3200 bspc config bottom_padding ${info.vnc.height - vncIframe.clientHeight} `
      + `&& dtach -n /run/unyw-dtach/xterm1 sh -c 'DISPLAY=:3200 xterm'`

 */
data class ProcessRequestScreen(val command: String = "", val socket: String ="", val width : Int = 0, val height : Int = 0)
val desktops = mutableListOf("default")

suspend fun processScreen (p : ApiParams){
    try {
        val tx = p.call.receiveText()
        val processRequest = gson.fromJson(tx, ProcessRequestScreen::class.java)

        var commands = mutableListOf<String>()

        if (processRequest.width != null && processRequest.height != null
            && processRequest.width > 20 && processRequest.height > 20
        ) {
            commands.add(
                "bspc config right_padding ${
                    p.ms.vncScreenSize.x - Math.min(
                        processRequest.width,
                        p.ms.vncScreenSize.x
                    )
                }"
            )
            commands.add(
                "bspc config bottom_padding ${
                    p.ms.vncScreenSize.y - Math.min(
                        processRequest.height,
                        p.ms.vncScreenSize.y
                    )
                }"
            )
        }

        if (processRequest.socket != null && processRequest.socket.isNotEmpty()) {
            if (desktops.indexOf(processRequest.socket) == -1) {
                desktops.add(processRequest.socket)
                commands.add("bspc monitor -a '${processRequest.socket}'")
            }
            commands.add("bspc desktop '${processRequest.socket}' -f")
        }
        if (processRequest.command != null && processRequest.command.isNotEmpty()) {
            commands.add(
                "dtach -n '/run/unyw/dtach/${processRequest.socket}' sh -c ${bashStr(
                    "echo $$ > /run/unyw/dtach-pids/${processRequest.socket};" + processRequest.command
                )}"
            )
        }

        val endc = commands.joinToString(separator = " && ")
        val res = ssh?.runCommand(endc)
        p.call.respond(mapOf("status" to "success", "result" to res))
    }catch(e :Exception){
        Log.e("UNYW_ERROR", e.toString())
        throw e;
    }
}


suspend fun processList (p : ApiParams){

    val list = withContext(Dispatchers.IO) {
        File(p.ms.filesDir.absolutePath + "/rootfs/run/unyw/dtach").list()
    }
    p.call.respond(mapOf("status" to "success", "list" to list))
}


data class ProcessRequestKill(val socket: String = "", val signal: String ="SIGKILL")

suspend fun processKill (p : ApiParams){
    val processRequest = gson.fromJson(p.call.receiveText(), ProcessRequestKill::class.java)

    p.call.respond(mapOf("status" to "success", "result" to ssh?.runCommand(
        "kill -${processRequest.signal} \$(cat '/run/unyw/dtach-pids/${processRequest.socket}')")
    ))
}