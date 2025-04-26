package com.example.playerdirectionarrow.network

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class ObserverUnsubscribePayload(
) : CustomPayload {

    override fun getId(): CustomPayload.Id<*> = ID

    companion object {
        val ID = CustomPayload.Id<ObserverUnsubscribePayload>(Identifier.of("dev.domkss.leadmethere", "unsubscribe_observer"))

        // Define a codec to write and read data
        val CODEC = object : PacketCodec<PacketByteBuf, ObserverUnsubscribePayload> {
            override fun encode(buf: PacketByteBuf, payload: ObserverUnsubscribePayload) {
            }

            override fun decode(buf: PacketByteBuf): ObserverUnsubscribePayload {
                return ObserverUnsubscribePayload()
            }
        }

        // Define the Type
        val TYPE: CustomPayload.Type<PacketByteBuf, ObserverUnsubscribePayload> = CustomPayload.Type(ID, CODEC)
    }
}
