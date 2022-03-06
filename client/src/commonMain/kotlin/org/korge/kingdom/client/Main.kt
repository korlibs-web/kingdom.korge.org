package org.korge.kingdom.client

import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korio.dynamic.*
import com.soywiz.korio.lang.*
import com.soywiz.korio.net.*
import com.soywiz.korio.net.ws.*
import com.soywiz.korio.util.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.korge.kingdom.shared.*
import org.korge.kingdom.shared.packet.*
import kotlin.coroutines.*

suspend fun main() = mainWithUrl { "ws://127.0.0.1:8080/" }

suspend fun mainWithUrl(block: suspend () -> String) = Korge {
    val url = block()

    text("Click to move")
    //solidRect(100, 100, Colors.RED)
    //println(Environment.getAll())
    //println(Json.encodeToString(Demo(10, "test")))

    var me: String? = null

    class Entity(val view: View) {
        val queue = AsyncThread()
    }

    val entities = LinkedHashMap<String, Entity>()

    val wsUrl = when {
        OS.isJs -> {
            val url = URL(Dyn.global["document"]["location"]["href"].str)
            val wsProtocol = if (url.isSecureScheme) "wss" else "ws"
            "$wsProtocol://${url.host}:${url.port}/"
        }
        else -> {
            Environment["GAME_WS_URL"] ?: url
        }
    }

    println("wsUrl=$wsUrl")

    val client = WebSocketClient(wsUrl) {
        onStringMessage {
            val packet = format.decodeFromString<Packet>(it)
            when (packet) {
                is Packet.Connected -> {
                    me = packet.entityId
                }
                is Packet.Appeared -> {
                    entities[packet.entityId] = Entity(solidRect(100, 100).xy(packet.x, packet.y))
                }
                is Packet.Disappeared -> {
                    entities.remove(packet.entityId)?.view?.removeFromParent()
                }
                is Packet.Moved -> {
                    val entity = entities[packet.entityId]
                    if (entity != null) {
                        val view = entity.view
                        entity.queue.cancel().sync(coroutineContext) {
                            view.tween(view::x[packet.x], view::y[packet.y])
                        }
                    }
                }
                else -> Unit
            }
            println("CLIENT RECEIVED: $packet")
        }
    }

    onClick {
        client.send(
            format.encodeToString(
                Packet.Move(
                    it.currentPosLocal.x.toInt(),
                    it.currentPosLocal.y.toInt(),
                )
            )
        )
    }
}
