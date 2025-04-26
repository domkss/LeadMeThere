package dev.domkss.leadmethere

import com.example.playerdirectionarrow.network.PlayerPosPayload
import io.netty.buffer.Unpooled
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.LoggerFactory

object LeadMeThere : ModInitializer {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
	private var tickCounter = 0

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerTickEvents.END_SERVER_TICK.register { server ->
			tickCounter++
			if (tickCounter >= 5) {  // Every 5 ticks (0.25 second)
				sendClosestPlayerPosToAllPlayers(server)
				tickCounter = 0  // Reset counter
			}
		}

		logger.info("Lead Me There Server Mod Loaded!")
	}


	// Method to find the closest player
	fun getClosestPlayer(player: ServerPlayerEntity, server: MinecraftServer): ServerPlayerEntity? {
		var closestPlayer: ServerPlayerEntity? = null
		var closestDistance = Double.MAX_VALUE

		// Iterate over all players to find the closest one
		for (otherPlayer in server.playerManager.playerList) {
			if (otherPlayer != player) {
				val distance = player.squaredDistanceTo(otherPlayer)
				if (distance < closestDistance) {
					closestDistance = distance
					closestPlayer = otherPlayer
				}
			}
		}

		return closestPlayer
	}

	// Server-side method to send the closest player's position to a player
	fun sendClosestPlayerPosToClient(player: ServerPlayerEntity, server: MinecraftServer) {
		// Get the closest player
		val closestPlayer = getClosestPlayer(player, server)

		// If we found the closest player, send their position
		if (closestPlayer != null) {
			val playerPos = PlayerPosPayload(closestPlayer.name.string, closestPlayer.pos)

			// Send the packet to the player
			ServerPlayNetworking.send(player, playerPos)
		}
	}

	// Method to send the position of the closest player to all players
	fun sendClosestPlayerPosToAllPlayers(server: MinecraftServer) {
		// Loop through all connected players and send the packet to each one
		for (player in server.playerManager.playerList) {
			sendClosestPlayerPosToClient(player as ServerPlayerEntity, server)
		}
	}



}