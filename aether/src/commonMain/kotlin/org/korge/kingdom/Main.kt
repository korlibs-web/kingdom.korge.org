package org.korge.kingdom

import org.korge.kingdom.server.*

suspend fun main() {
    org.korge.kingdom.client.mainWithUrl {
        val actualPort = startServer(host = "127.0.0.1", port = 0).port
        "ws://127.0.0.1:$actualPort/"
    }
}
