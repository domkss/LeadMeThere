/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere

import dev.domkss.leadmethere.gui.OpenTargetPlayerSelectorKey
import dev.domkss.leadmethere.gui.PlayerListScreen
import dev.domkss.leadmethere.hud.TargetDirectionHUDRenderer
import dev.domkss.leadmethere.network.PlayerPosPayload
import dev.domkss.leadmethere.xray.TargetPlayerOutlineRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory
import net.minecraft.client.util.InputUtil
import net.minecraft.client.option.KeyBinding

object LeadMeThereClient : ClientModInitializer {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)


    var trackedPlayer: PlayerPosPayload? = null
    private var lastPacketReceivedTick: Int = 0

    override fun onInitializeClient() {
        // Register the receiver for the custom payload
        ClientPlayNetworking.registerGlobalReceiver(PlayerPosPayload.ID) { playerPosPayload: PlayerPosPayload, context: ClientPlayNetworking.Context ->

            // Ensure the code runs on the main client thread to interact with Minecraft
            MinecraftClient.getInstance().execute {
                trackedPlayer = playerPosPayload
                lastPacketReceivedTick = MinecraftClient.getInstance().world?.time?.toInt() ?: 0
            }
        }

        //Reset target if no position received for 2 seconds
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (trackedPlayer != null) {
                val currentTick = client.world?.time?.toInt() ?: return@EndTick
                if (currentTick - lastPacketReceivedTick > 40) {
                    trackedPlayer = null
                }
            }
        })

        //Register Direction Display HUD Renderer
        TargetDirectionHUDRenderer.register()

        //Register Target Player Outline Renderer
        TargetPlayerOutlineRenderer.register()

        //Register Key Binding to open player selector
        OpenTargetPlayerSelectorKey.register()


        // Add a tick event listener to check if key is pressed
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
            if (OpenTargetPlayerSelectorKey.getKeyBinding()?.wasPressed() == true) {
                MinecraftClient.getInstance().setScreen(PlayerListScreen(trackedPlayer?.name))
            }

            // Remove the default Tab Keybinding if it's the default
            if (client?.options != null) {
                if (MinecraftClient.getInstance().options.playerListKey.isDefault) {
                    MinecraftClient.getInstance().options.playerListKey.setBoundKey(InputUtil.UNKNOWN_KEY)
                    KeyBinding.updateKeysByCode()
                }
            }
        })


        logger.info("Lead Me There Client Mod Loaded!")
    }


}