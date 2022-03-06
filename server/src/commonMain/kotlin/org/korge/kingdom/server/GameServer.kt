package org.korge.kingdom.server

import com.soywiz.klock.*
import com.soywiz.korio.async.*
import com.soywiz.korio.lang.*
import com.soywiz.korio.net.http.*
import com.soywiz.korio.util.*
import kotlinx.serialization.*
import org.korge.kingdom.shared.packet.*
import kotlin.coroutines.*

class GameServer {
    class Client(val request: HttpServer.WsRequest) {
        val id = UUID.randomUUID().toString()
        var x: Int = 0
        var y: Int = 0
    }

    val clients = LinkedHashSet<Client>()

    inline fun <reified T : Packet> Client.send(packet: T) {
        request.send(format.encodeToString<T>(packet))
    }

    inline fun <reified T : Packet> sendAll(packet: T) {
        for (client in clients) {
            client.request.send(format.encodeToString<T>(packet))
        }
    }

    suspend fun websocketHandler(req: HttpServer.WsRequest) {
        val client = Client(req)

        //withContext(context) {
        run {
            clients.add(client)
            println("THREAD[$currentThreadId]: Client[${client.request.address}] connected: $client")
        }

        req.onClose {
            //withContext(context) {
            run {
                clients.remove(client)
                for (c in clients) {
                    c.send(Packet.Disappeared(client.id))
                }
                println("THREAD[$currentThreadId]: Client[${client.request.address}] disconnected: $client ($it)")
            }
        }

        req.onStringMessage {
            //withContext(context) {
            run {
                val packet = format.decodeFromString<Packet>(it)
                //req.send(it)
                println("THREAD[$currentThreadId]: SERVER RECEIVED PACKET: $packet")
                when (packet) {
                    is Packet.Say -> {
                        sendAll(Packet.Said(client.id, packet.message))
                    }
                    is Packet.Move -> {
                        client.x = packet.x
                        client.y = packet.y
                        sendAll(Packet.Moved(client.id, client.x, client.y))
                    }
                }

            }
        }

        // On connected
        launchImmediately(coroutineContext) {
            delay(1.milliseconds) // @TODO: This shouldn't be necessary
            client.send(Packet.Connected(client.id))
            sendAll(Packet.Appeared(client.id, client.x, client.y))
            for (c in clients) {
                if (c !== client) {
                    client.send(Packet.Appeared(c.id, c.x, c.y))
                }
            }
        }
    }
}
