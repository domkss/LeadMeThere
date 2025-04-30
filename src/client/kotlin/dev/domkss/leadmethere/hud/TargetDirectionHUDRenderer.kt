/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere.hud

import com.mojang.blaze3d.systems.RenderSystem
import dev.domkss.leadmethere.LeadMeThereClient.trackedPlayer
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

object TargetDirectionHUDRenderer {

    private val DIRECTION_ARROW_TEXTURE = Identifier.of("leadmethere", "textures/gui/arrow.png")

    fun register() =
        HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { layeredDrawer: LayeredDrawerWrapper ->
            layeredDrawer.attachLayerBefore(
                IdentifiedLayer.CHAT,
                DIRECTION_ARROW_TEXTURE,
                this::renderDirectionHUD
            )
        })

    private fun renderDirectionHUD(context: DrawContext, tickCounter: RenderTickCounter) {
        val trackedPlayer = trackedPlayer
        val client = MinecraftClient.getInstance()
        val currentPlayer = client.player ?: return
        val targetPos = trackedPlayer?.pos ?: return

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
                { texture -> RenderLayer.getGuiTextured(DIRECTION_ARROW_TEXTURE) },
                DIRECTION_ARROW_TEXTURE,
                centerX - arrowSize / 2,
                topY - arrowSize / 2,
                0f, 0f,
                arrowSize, arrowSize,
                128, 128,
                128, 128
            )


            val textRenderer = MinecraftClient.getInstance().textRenderer
            val nameTextWidth = textRenderer.getWidth(trackedPlayer.name)
            val playerName = trackedPlayer.name

            // Display Tracked Player Name
            context.drawText(
                textRenderer,
                playerName,
                centerX - nameTextWidth / 2,
                topY + arrowSize / 2 + 2,
                0xFFFFFF, // color (white)
                true
            )

            val toTargetY = targetPos.y - playerPos.y
            val distanceToTarget:Int = ceil(sqrt(toTargetX * toTargetX + toTargetY * toTargetY + toTargetZ * toTargetZ)).toInt()
            val distanceText ="${distanceToTarget}m"
            val distanceTextWidth = textRenderer.getWidth(distanceText)

            // Display Distance To Target Player
            context.drawText(
                textRenderer,
                distanceText,
                centerX - distanceTextWidth / 2,
                topY + arrowSize / 2 + 12,
                0xFFFFFF, // color (white)
                true
            )

        }

    }

}