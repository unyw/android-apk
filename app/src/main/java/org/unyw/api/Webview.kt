package org.unyw.api

import io.ktor.request.*
import io.ktor.response.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.unyw.mainActivity
import java.lang.Exception

data class WebviewRequest(val command: String)

suspend fun webviewEval (p : ApiParams){
    val webviewRequest = gson.fromJson(p.call.receiveText(), WebviewRequest::class.java)
    val onResultChannel = Channel<String>()

    if(mainActivity != null){
        mainActivity?.webView?.post {
            mainActivity?.webView?.evaluateJavascript(webviewRequest.command) {
                runBlocking {
                    onResultChannel.send(it)
                }
            }
        }
        p.call.respond(mapOf("status" to "success", "result" to onResultChannel.receive()))
    }
}
