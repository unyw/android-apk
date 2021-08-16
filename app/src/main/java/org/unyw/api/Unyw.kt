package org.unyw.api

import io.ktor.response.*

var apiVersion = "1.0.0"

suspend fun unywInfo (p : ApiParams){
    val list : MutableMap<String, List<String>> = mutableMapOf()
    api.forEach{ module ->
        list[module.key] = module.value.map { function -> function.key }
    }

    val vnc = mapOf(
        "token" to p.ms.tokens["UNYW_TOKEN_VNC"],
        "width" to p.ms.vncScreenSize.x,
        "height" to p.ms.vncScreenSize.y,
    )

    p.call.respond(mapOf("status" to "success", "version" to apiVersion, "api" to list, "vnc" to vnc, "tokens" to p.ms.tokens))
}
