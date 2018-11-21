package io.ghostbuster91.ktm.notifier.jitpack

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain

@Suppress("UNUSED")
fun Application.module() {
    val client = HttpClient()
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    routing {
        post("/demo") {
            val event = call.receive<Map<String,Any>>()
            println(event)
            val repository = event["repository"] as Map<String,Any>?
            if(repository != null){
                val buildLog = client.get<String>("https://jitpack.io/com/github/${repository["fullName"]}/${event["after"]}")
                println("start")
                println(buildLog)
                println("end")
                call.respond(HttpStatusCode.Created)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}


fun main(args: Array<String>) = EngineMain.main(args)

data class PushEvent(val repository: Repository, val after: String)
data class Repository(val fullName: String)