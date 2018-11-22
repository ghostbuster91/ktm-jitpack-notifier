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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

typealias JSON = Map<String,Any?>
val inbox = Channel<JSON>(capacity = Channel.UNLIMITED)

val client = HttpClient(OkHttp) {
    install(HttpPlainText) {
        defaultCharset = Charsets.UTF_8
    }
    engine {
        followRedirects = true
        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
        config {
            followRedirects(true)
            readTimeout(5, TimeUnit.MINUTES)
        }
    }
    defaultRequest {
        header("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
    }
}

@Suppress("UNUSED")
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    routing {
        post("/demo") {
            val event = call.receive<Map<String, Any>>()
            println(ObjectMapper().writeValueAsString(event))
            println("Sending to inbox...")
            inbox.send(event)
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun main(args: Array<String>) {
    EngineMain.main(args)
    runBlocking {
        for (msg in inbox){
            println("Received a message")
            println("Waiting 20 seconds...")
            delay(20_000)
            val refTagRegex = "refs/tags/(.+)".toRegex()
            val isRelease = (msg["ref"] as? String)?.matches(refTagRegex) ?: false
            val repository = msg["repository"] as JSON?
            if (repository != null) {
                println("Processing repository: $repository")
                val version = if (isRelease) {
                    val matcher = refTagRegex.find((msg["ref"] as String))
                    val version = matcher!!.groups[1]!!.value
                    version
                } else {
                    msg["after"]
                }
                client.get<String>("https://jitpack.io/com/github/${repository["full_name"]}/$version")
            }
        }
    }
}

