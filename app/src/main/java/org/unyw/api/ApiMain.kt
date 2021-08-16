package org.unyw.api

import android.util.Log
import com.google.gson.Gson
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

import org.unyw.MainService
import java.security.MessageDigest


class ApiParams(val ms : MainService, val call: ApplicationCall)

var gson = Gson()

var api : Map<String, Map<String, suspend (ApiParams) -> Unit>> = mapOf(
    "device" to mapOf(
        "torch" to ::deviceTorch,
        "battery" to ::deviceBattery,
    ),
    "file" to mapOf(
        "read" to ::fileRead,
        "write" to ::fileWrite,
        "list" to ::fileList,
    ),
    "intent" to mapOf(
        "authenticate" to ::intentAuthenticate,
        "photo" to ::intentPhoto,
        "open" to ::intentOpen,
        "filepicker" to ::intentFilepicker,
    ),
    "notification" to mapOf(
        "toast" to ::notificationToast,
        "vibrate" to ::notificationVibrate,
        "show" to ::notificationShow,
    ),
    "process" to mapOf(
        "run" to ::processRun,
        "dtach" to ::processDtach,
        "screen" to ::processScreen,
        "list" to ::processList,
        "kill" to ::processKill,
    ),
    "unyw" to mapOf(
        "info" to ::unywInfo,
    ),
    "webview" to mapOf(
        "eval" to ::webviewEval,
    ),
)







fun startApi(ms : MainService){
    embeddedServer(Netty, 12080) {
        install(ContentNegotiation) {
            gson {}
        }
        install(StatusPages) {
            exception<Throwable> { cause ->
                Log.d("UNYW_ERROR",cause.toString())
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        install(CORS) {
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Delete)
            method(HttpMethod.Patch)
            header(HttpHeaders.AccessControlAllowHeaders)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.AccessControlAllowOrigin)
            allowCredentials = true

            anyHost()
        }

        routing {
            post("/{module}/{function}") {
                // Check if localhost (or isDebug)
                if (!ms.debug && call.request.host() != "localhost") {
                    Log.d("UNYW_SERVER", call.request.host())
                    call.respond(
                        HttpStatusCode.Forbidden,
                        "error" to "only request from localhost are allowed"
                    )
                    return@post
                }

                // Check if token is right
                if(call.request.queryParameters["token"] == null || !MessageDigest.isEqual(
                        call.request.queryParameters["token"]!!.toByteArray(), ms.tokens["UNYW_TOKEN_API"]!!.toByteArray())
                ) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("status" to "error: wrong token!")
                    )
                    return@post
                }

                // Check if requested path exists
                val module = call.parameters["module"]
                val function = call.parameters["function"]
                if(!api.containsKey(module) || !api[module]!!.containsKey(function)){
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("status" to "error: function $module/$function not found!")
                    )
                    return@post
                }

                api[module]!![function]!!(ApiParams(ms, call))
                return@post
            }
        }
    }.start(wait = true)
}