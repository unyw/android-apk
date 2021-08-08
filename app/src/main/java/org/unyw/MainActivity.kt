package org.unyw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.mainWebView)
        webView.settings.setJavaScriptEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }
        }

        embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                gson {}
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
                post("*") {
                    if(call.request.host() !== "localhost"){
                        call.respond(HttpStatusCode.Forbidden, "error" to "only request from localhost are allowed")
                        return@post
                    }
                    var a =  call.receiveText()
                    Log.d("HELLO",a)
                    call.respond(mapOf("message" to call.request.path(), "ms2" to a))
                }
            }
        }.start(wait = true)

        webView.loadUrl("https://www.google.com/")

    }
}