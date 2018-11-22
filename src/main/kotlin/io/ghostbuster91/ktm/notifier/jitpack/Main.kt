package io.ghostbuster91.ktm.notifier.jitpack

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.HttpPlainText
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.delay
import okhttp3.logging.HttpLoggingInterceptor

@Suppress("UNUSED")
fun Application.module() {
    val client = HttpClient(OkHttp) {
        install(HttpPlainText) {
            defaultCharset = Charsets.UTF_8
        }
        engine {
            followRedirects = true
            addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
            config {
                followRedirects(true)
            }
        }
        defaultRequest {
            header("user-agent", MOZILLA_USER_AGENT)
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
            println(ObjectMapper().writeValueAsString(event))
            delay(5000)
            val refTagRegex = "refs/tags/(.+)".toRegex()
            val isRelease = (event["ref"] as? String)?.matches(refTagRegex) ?: false
            val repository = event["repository"] as Map<String, Any>?
            if (repository != null) {
                val version = if (isRelease) {
                    val matcher = refTagRegex.find((event["ref"] as String))
                    val version = matcher!!.groups[1]!!.value
                    version
                } else {
                    event["after"]
                }
                client.get<String>("https://jitpack.io/com/github/${repository["full_name"]}/$version")
                call.respond(HttpStatusCode.Created)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun main(args: Array<String>) = EngineMain.main(args)

val MOZILLA_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36"