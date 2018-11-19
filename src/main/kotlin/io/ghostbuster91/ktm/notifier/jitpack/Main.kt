package io.ghostbuster91.ktm.notifier.jitpack

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.net.URL

fun main(args: Array<String>) {
    val port = Integer.valueOf(System.getenv("PORT"))
    val server = embeddedServer(Netty, port = port) {
        routing {
            post("/demo") {
                val event = call.receive<PushEvent>()
                println(event)
                val buildLog = URL("https://jitpack.io/com/github/${event.repository.fullName}/${event.after}").readText()
                println(buildLog)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
    server.start(wait = true)
}

data class PushEvent(val repository: Repository,val after:String)
data class Repository(val fullName: String)