package dev.domkss.leadmethere

import com.example.playerdirectionarrow.network.PlayerPosPayload
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.*
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.render.RenderPhase.LineWidth
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.util.Identifier
import net.minecraft.util.math.*
import net.minecraft.world.World
import org.joml.Matrix4f
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.abs


object LeadMeThereClient : ClientModInitializer {

	private val logger = LoggerFactory.getLogger(this.javaClass.name)
	private val ARROW_TEXTURE = Identifier.of("leadmethere", "textures/gui/arrow.png")

	var trackedPlayer:PlayerPosPayload? =null


	override fun onInitializeClient() {
		// First, register the payload
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


		HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { layeredDrawer: LayeredDrawerWrapper ->
			layeredDrawer.attachLayerBefore(
				IdentifiedLayer.CHAT,
				ARROW_TEXTURE,
				LeadMeThereClient::renderHUD
			)
		})



		WorldRenderEvents.AFTER_ENTITIES.register(WorldRenderEvents.AfterEntities { context ->
			val pos = trackedPlayer?.pos ?: return@AfterEntities
			val world = MinecraftClient.getInstance().world ?: return@AfterEntities
			val camera = context.camera()
			if(!shouldDrawPlayerOutlineBox(world,camera.pos,pos))return@AfterEntities


			val matrices = context.matrixStack()
			matrices?.push() ?: return@AfterEntities

			val vertexConsumers = context.consumers()


			val relativeX = pos.x - camera.pos.x
			val relativeY = pos.y - camera.pos.y
			val relativeZ = pos.z - camera.pos.z

			matrices.translate(relativeX, relativeY, relativeZ)

			val vertexConsumer = vertexConsumers?.getBuffer(XRAY_LINES_LAYER) ?:  return@AfterEntities

			val box = Box(-0.5, 0.0, -0.5, 0.5, 1.8, 0.5)
			drawPlayerOutlineBox(matrices.peek().positionMatrix, vertexConsumer, box, 1.0f, 0.0f, 0.0f, 1.0f)

			matrices.pop()

		})


		logger.info("Lead Me There Client Mod Loaded!")
	}


	val XRAY_LINES_LAYER = RenderLayer.of(
		"player_xray_outlines",
		VertexFormats.LINES,
		DrawMode.LINES,
		1536,
		RenderLayer.MultiPhaseParameters.builder()
			.program(RenderPhase.LINES_PROGRAM)
			.lineWidth(LineWidth(OptionalDouble.of(2.0)))
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.target(RenderPhase.ITEM_ENTITY_TARGET)
			.depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			.writeMaskState(RenderPhase.ALL_MASK)
			.cull(RenderPhase.DISABLE_CULLING)
			.build(false));



	private fun drawPlayerOutlineBox(
		matrix: Matrix4f,
		vertexConsumer: VertexConsumer,
		box: Box,
		r: Float, g: Float, b: Float, a: Float
	) {
		val minX = box.minX.toFloat()
		val minY = box.minY.toFloat()
		val minZ = box.minZ.toFloat()
		val maxX = box.maxX.toFloat()
		val maxY = box.maxY.toFloat()
		val maxZ = box.maxZ.toFloat()

		fun vertex(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float) {
			vertexConsumer.vertex(matrix, x, y, z)
				.color(r, g, b, a)
				.normal(nx, ny, nz)
		}

		// Bottom
		vertex(minX, minY, minZ, 0f, 1f, 0f); vertex(maxX, minY, minZ, 0f, 1f, 0f)
		vertex(maxX, minY, minZ, 0f, 1f, 0f); vertex(maxX, minY, maxZ, 0f, 1f, 0f)
		vertex(maxX, minY, maxZ, 0f, 1f, 0f); vertex(minX, minY, maxZ, 0f, 1f, 0f)
		vertex(minX, minY, maxZ, 0f, 1f, 0f); vertex(minX, minY, minZ, 0f, 1f, 0f)

		// Top
		vertex(minX, maxY, minZ, 0f, 1f, 0f); vertex(maxX, maxY, minZ, 0f, 1f, 0f)
		vertex(maxX, maxY, minZ, 0f, 1f, 0f); vertex(maxX, maxY, maxZ, 0f, 1f, 0f)
		vertex(maxX, maxY, maxZ, 0f, 1f, 0f); vertex(minX, maxY, maxZ, 0f, 1f, 0f)
		vertex(minX, maxY, maxZ, 0f, 1f, 0f); vertex(minX, maxY, minZ, 0f, 1f, 0f)

		// Sides
		vertex(minX, minY, minZ, 0f, 1f, 0f); vertex(minX, maxY, minZ, 0f, 1f, 0f)
		vertex(maxX, minY, minZ, 0f, 1f, 0f); vertex(maxX, maxY, minZ, 0f, 1f, 0f)
		vertex(maxX, minY, maxZ, 0f, 1f, 0f); vertex(maxX, maxY, maxZ, 0f, 1f, 0f)
		vertex(minX, minY, maxZ, 0f, 1f, 0f); vertex(minX, maxY, maxZ, 0f, 1f, 0f)
	}

	fun shouldDrawPlayerOutlineBox(world: World, cameraPos: Vec3d, targetPos: Vec3d): Boolean {
		val targetPosCenter=targetPos.add(0.0,1.0,0.0)


		val direction = targetPosCenter.subtract(cameraPos).normalize()
		val distance = cameraPos.distanceTo(targetPosCenter)

		val stepSize = 0.5
		var traveled = 0.0

		while (traveled < distance) {
			val currentPos = cameraPos.add(direction.multiply(traveled))
			val blockPos = BlockPos(currentPos.x.toInt(), currentPos.y.toInt(), currentPos.z.toInt())
			val blockState = world.getBlockState(blockPos)


				if (!blockState.isTransparent) {
					// Found a solid block between camera and target position
					return true
				}
			traveled += stepSize
		}

		// No solid block found - only transparent blocks between camera and target position
		return false
	}



	private fun renderHUD(context: DrawContext, tickCounter: RenderTickCounter) {
		val trackedPlayer= trackedPlayer
		val client = MinecraftClient.getInstance()
		val currentPlayer = client.player ?: return
		val targetPos= trackedPlayer?.pos ?: return

		val playerPos = currentPlayer.pos
		val playerYaw = currentPlayer.yaw

		// Calculate direction from player to target, ignoring Y
		val toTargetX = targetPos.x - playerPos.x
		val toTargetZ = targetPos.z - playerPos.z

		val angleToTarget = MathHelper.atan2(toTargetZ, toTargetX) * (180.0 / Math.PI) - 90.0

		// Normalize angles to [-180, 180] range
		val relativeAngle = MathHelper.wrapDegrees(angleToTarget - playerYaw)

		// Set a tolerance, 30 deg
		val tolerance = 30.0

		if (abs(relativeAngle) <= tolerance) {
			val centerX = context.scaledWindowWidth / 2
			val topY = context.scaledWindowHeight / 6

			val arrowSize = 8

			RenderSystem.enableBlend()

			context.drawTexture(
				{ texture -> RenderLayer.getGuiTextured(ARROW_TEXTURE) },
				ARROW_TEXTURE,
				centerX - arrowSize / 2,
				topY - arrowSize / 2,
				0f, 0f,
				arrowSize, arrowSize,
				128,128,
				128, 128
			)


			val textRenderer = MinecraftClient.getInstance().textRenderer
			val textWidth = textRenderer.getWidth(trackedPlayer.name)
			val playerName= trackedPlayer.name

			// Display Tracked Player Name
			context.drawText(
				textRenderer,
				playerName,
				centerX - textWidth / 2,
				topY + arrowSize / 2 + 2,
				0xFFFFFF, // color (white)
				true
			)
		}

	}


}