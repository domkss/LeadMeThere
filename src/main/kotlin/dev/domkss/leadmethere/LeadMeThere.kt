package dev.domkss.leadmethere

import com.example.playerdirectionarrow.network.ObserverSubscribePayload
import com.example.playerdirectionarrow.network.ObserverUnsubscribePayload
import dev.domkss.leadmethere.PlayerObserverManager.subscribe
import dev.domkss.leadmethere.PlayerObserverManager.unSubscribe
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.LoggerFactory


object LeadMeThere : ModInitializer {
    private val logger = LoggerFactory.getLogger(this.javaClass.name)


    override fun onInitialize() {

        // Register the custom payloads
        PayloadTypeRegistry.playC2S().register(ObserverSubscribePayload.ID, ObserverSubscribePayload.CODEC)
        PayloadTypeRegistry.playC2S().register(ObserverUnsubscribePayload.ID, ObserverUnsubscribePayload.CODEC)

        // Register the receiver for the custom payloads
        ServerPlayNetworking.registerGlobalReceiver(ObserverSubscribePayload.ID) { observerSubscribePayload: ObserverSubscribePayload, context: ServerPlayNetworking.Context ->
            val player = context.player()
            player.server.execute {
                subscribe(context.player(), observerSubscribePayload.name)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(ObserverUnsubscribePayload.ID) { observerUnsubscribePayload: ObserverUnsubscribePayload, context: ServerPlayNetworking.Context ->
            val player = context.player()
            player.server.execute {
                unSubscribe(context.player())
            }
        }

        var tickCounter = 0
        //Send the data to observers
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (tickCounter > 5) {
                PlayerObserverManager.tick(server);
                tickCounter = 0
            } else tickCounter++
        }


        logger.info("Lead Me There Server Mod Loaded!")
    }


}