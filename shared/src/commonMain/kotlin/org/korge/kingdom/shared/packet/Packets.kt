package org.korge.kingdom.shared.packet

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
@JsonClassDiscriminator("type")
sealed class Packet(val type: String) {
//sealed class Packet {
    @Serializable
    @SerialName("move")
    data class Move(val x: Int, val y: Int) : Packet("move")

    @Serializable
    @SerialName("say")
    data class Say(val message: String) : Packet("say")

    @Serializable
    @SerialName("said")
    data class Said(val entityId: String, val message: String) : Packet("said")

    @Serializable
    @SerialName("connected")
    data class Connected(val entityId: String) : Packet("connected")

    @Serializable
    @SerialName("appeared")
    data class Appeared(val entityId: String, val x: Int, val y: Int) : Packet("appeared")

    @Serializable
    @SerialName("disappeared")
    data class Disappeared(val entityId: String) : Packet("disappeared")

    @Serializable
    @SerialName("moved")
    data class Moved(val entityId: String, val x: Int, val y: Int) : Packet("moved")
}

/*
val packetModule = SerializersModule {
    polymorphic(Packet::class) {
        subclass(Packet.MovePacket::class)
    }
}

 */

val format = Json { }
