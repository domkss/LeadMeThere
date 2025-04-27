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

class ObserverUnsubscribePayload(
) : CustomPayload {

    override fun getId(): CustomPayload.Id<*> = ID

    companion object {
        val ID = CustomPayload.Id<ObserverUnsubscribePayload>(
            Identifier.of(
                "dev.domkss.leadmethere",
                "unsubscribe_observer"
            )
        )

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
