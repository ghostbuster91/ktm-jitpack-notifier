package io.ghostbuster91.ktm.notifier.jitpack

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import java.net.URL

@Suppress("UNUSED")
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    routing {
        post("/demo") {
            val event = call.receive<Map<String,Any>>()
            println(event)
            val buildLog = URL("https://jitpack.io/com/github/${(event["repository"] as Map<String,Any>)["fullName"]}/${event["after"]}").readText()
            println("start")
            println(buildLog)
            println("end")
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun main(args: Array<String>) = EngineMain.main(args)

data class PushEvent(val repository: Repository, val after: String)
data class Repository(val fullName: String)