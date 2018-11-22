package io.ghostbuster91.ktm.notifier.jitpack

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.HttpPlainText
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import okhttp3.logging.HttpLoggingInterceptor

@Suppress("UNUSED")
fun Application.module() {
    val client = HttpClient(OkHttp) {
        install(HttpPlainText) {
            defaultCharset = Charsets.UTF_8
        }
        engine {
            addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
        }
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    routing {
        post("/demo") {
            val event = call.receive<Map<String, Any>>()
            println(event)
            val repository = event["repository"] as Map<String, Any>?
            if (repository != null) {
                client.request<String> {
                    header("user-agent", MOZILLA_USER_AGENT)
                    url("https://jitpack.io/com/github/${repository["fullName"]}/${event["after"]}")
                }
                call.respond(HttpStatusCode.Created)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun main(args: Array<String>) = EngineMain.main(args)

data class PushEvent(val repository: Repository, val after: String)
data class Repository(val fullName: String)

val MOZILLA_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36"