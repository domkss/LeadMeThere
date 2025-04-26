package dev.domkss.leadmethere

import com.example.playerdirectionarrow.network.PlayerPosPayload
import dev.domkss.leadmethere.gui.OpenTargetPlayerSelectorKey
import dev.domkss.leadmethere.gui.PlayerListScreen
import dev.domkss.leadmethere.hud.TargetDirectionHUDRenderer
import dev.domkss.leadmethere.xray.TargetPlayerOutlineRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import org.slf4j.LoggerFactory


object LeadMeThereClient : ClientModInitializer {

	private val logger = LoggerFactory.getLogger(this.javaClass.name)


	var trackedPlayer:PlayerPosPayload? =null


	override fun onInitializeClient() {
		// Register the custom payload
		PayloadTypeRegistry.playS2C().register(PlayerPosPayload.ID, PlayerPosPayload.CODEC)

		// Register the receiver for the custom payload
		ClientPlayNetworking.registerGlobalReceiver(PlayerPosPayload.ID) { playerPosPayload: PlayerPosPayload, context: ClientPlayNetworking.Context ->

			// Ensure the code runs on the main client thread to interact with Minecraft
			MinecraftClient.getInstance().execute {
				trackedPlayer=playerPosPayload

				// Now we have the decoded payload directly (playerPosPayload)
				logger.info("Received position of ${playerPosPayload.name}: ${playerPosPayload.pos}")


			}
		}

		//Register Direction Display HUD Renderer
		TargetDirectionHUDRenderer.register()

		//Register Target Player Outline Renderer
		TargetPlayerOutlineRenderer.register()

		//Register Key Binding to open player selector
		OpenTargetPlayerSelectorKey.register()


		// Add a tick event listener to check if key is pressed
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient? ->
			if (OpenTargetPlayerSelectorKey.getKeyBinding()?.wasPressed() == true) {
				MinecraftClient.getInstance().setScreen(PlayerListScreen())
			}
		})


		logger.info("Lead Me There Client Mod Loaded!")
	}











}