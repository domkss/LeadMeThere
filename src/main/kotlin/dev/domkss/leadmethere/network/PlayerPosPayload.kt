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
import net.minecraft.util.math.Vec3d

class PlayerPosPayload(
    val name: String,
    val pos: Vec3d
) : CustomPayload {

    override fun getId(): CustomPayload.Id<*> = ID

    companion object {
        val ID = CustomPayload.Id<PlayerPosPayload>(Identifier.of("dev.domkss.leadmethere", "target_player_pos"))

        // Define a codec to write and read data
        val CODEC = object : PacketCodec<PacketByteBuf, PlayerPosPayload> {
            override fun encode(buf: PacketByteBuf, payload: PlayerPosPayload) {
                buf.writeString(payload.name)
                buf.writeDouble(payload.pos.x)
                buf.writeDouble(payload.pos.y)
                buf.writeDouble(payload.pos.z)
            }

            override fun decode(buf: PacketByteBuf): PlayerPosPayload {
                val name = buf.readString(32767)
                val x = buf.readDouble()
                val y = buf.readDouble()
                val z = buf.readDouble()
                return PlayerPosPayload(name, Vec3d(x, y, z))
            }
        }

        // Define the Type
        val TYPE: CustomPayload.Type<PacketByteBuf, PlayerPosPayload> = CustomPayload.Type(ID, CODEC)
    }
}
