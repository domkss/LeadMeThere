package com.example.playerdirectionarrow.network

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class ObserverSubscribePayload(
    val name: String,
) : CustomPayload {

    override fun getId(): CustomPayload.Id<*> = ID

    companion object {
        val ID = CustomPayload.Id<ObserverSubscribePayload>(Identifier.of("dev.domkss.leadmethere", "subscribe_observer"))

        // Define a codec to write and read data
        val CODEC = object : PacketCodec<PacketByteBuf, ObserverSubscribePayload> {
            override fun encode(buf: PacketByteBuf, payload: ObserverSubscribePayload) {
                buf.writeString(payload.name)
            }

            override fun decode(buf: PacketByteBuf): ObserverSubscribePayload {
                val name = buf.readString(32767)
                return ObserverSubscribePayload(name)
            }
        }

        // Define the Type
        val TYPE: CustomPayload.Type<PacketByteBuf, ObserverSubscribePayload> = CustomPayload.Type(ID, CODEC)
    }
}
