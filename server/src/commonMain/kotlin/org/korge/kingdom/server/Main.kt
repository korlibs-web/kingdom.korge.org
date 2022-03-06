package org.korge.kingdom.server

import com.soywiz.klock.*
import com.soywiz.klogger.*
import com.soywiz.korio.*
import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.*
import com.soywiz.korio.net.*
import com.soywiz.korio.net.http.*
import com.soywiz.korio.util.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

fun main() = Korio {
    val url = startServer(host = "0.0.0.0", port = 8080)
    openBrowser(url)
    while (true) {
        com.soywiz.korio.async.delay(1.seconds)
    }
}

suspend fun startServer(host: String, port: Int): URL {
    val rootUnjailed =
        localCurrentDirVfs["www"].takeIfExists()
            ?: localCurrentDirVfs["client/build/www"].takeIfExists()
            ?: localCurrentDirVfs["../client/build/www"]
    val root = rootUnjailed.jail()
    //val context = createSingleThreadedDispatcher()
    val context = coroutineContext
    val gameServer = GameServer()

    println("Serving...$root")
    println("Starting server...")
    //launch(gameServer.context) {
    val server = createHttpServer {
        errorHandler {
            Console.error("startServer.createHttpServer.errorHandler: ${it.message}")
            //it.printStackTrace()
        }
        httpHandler {
            val file = root[it.uri]
            val rfile = if (file.isDirectory()) file["index.html"] else file
            if (rfile.exists()) {
                it.addHeader("Content-Type", MimeType.getByExtension(rfile.extensionLC).mime)
                it.end(rfile)
                //it.end(rfile.readBytes())
            } else {
                it.setStatus(404)
                it.addHeader("Content-Type", "text/html")
                it.end("Not found: $rfile")
            }
        }

        withContext(context) {
            //println(currentThreadId)
            websocketHandler { req ->
                //println(currentThreadId)
                gameServer.websocketHandler(req)
            }
        }
        listen(port, host)
    }
    val httpUrl = URL("http://${server.actualHost}:${server.actualPort}/")
    println("Listening at $httpUrl")
    //CreateDefaultGameWindow().browse(URL(httpUrl))
    return httpUrl
}

suspend fun openBrowser(url: URL) {
    when {
        OS.isWindows -> localCurrentDirVfs.execProcess("cmd", "/c", "rundll32", "url.dll,FileProtocolHandler", url.toString())
        OS.isLinux -> localCurrentDirVfs.execProcess("xdg-open", url.toString())
        else -> localCurrentDirVfs.execProcess("open", url.toString())
    }
}
