/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere.network

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class ObserverSubscribePayload(
    val name: String,
) : CustomPayload {

    override fun getId(): CustomPayload.Id<*> = ID

    companion object {
        val ID =
            CustomPayload.Id<ObserverSubscribePayload>(Identifier.of("dev.domkss.leadmethere", "subscribe_observer"))

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
