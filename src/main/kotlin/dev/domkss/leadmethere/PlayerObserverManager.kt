package dev.domkss.leadmethere

import com.example.playerdirectionarrow.network.PlayerPosPayload
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*


object PlayerObserverManager {

    // Maps observing player UUID -> target player name
    private val observers: MutableMap<UUID, String> = HashMap()


    fun subscribe(observer: ServerPlayerEntity, targetName: String) {
        observers[observer.uuid] = targetName
    }

    fun unSubscribe(observer: ServerPlayerEntity){
        observers.remove(observer.uuid)
    }

    //Send the target player position to their respective observer
    fun tick(server: MinecraftServer) {
        for (world in server.worlds) {
            for (observer in observers) {
                val observerPlayer = server.playerManager.getPlayer(observer.key)
                if(observerPlayer==null || !observerPlayer.isAlive) return

                val targetName = observer.value
                val targetPlayer = world.players.firstOrNull { it -> it.gameProfile.name.equals(targetName, ignoreCase = true) }

                if (targetPlayer == null || !targetPlayer.isAlive || targetPlayer.isDisconnected) {
                    continue  // Target does not exist in this world or is dead
                }

                val playerPos = PlayerPosPayload(targetName, targetPlayer.pos)

                // Send the packet to the player
                ServerPlayNetworking.send(observerPlayer, playerPos)

            }

        }
    }


}