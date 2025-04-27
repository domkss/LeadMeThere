/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere.xray

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode
import dev.domkss.leadmethere.LeadMeThereClient.trackedPlayer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gl.UniformType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.RenderPhase.LineWidth
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Matrix4f
import java.util.*


object TargetPlayerOutlineRenderer {


    fun register() = WorldRenderEvents.AFTER_ENTITIES.register(WorldRenderEvents.AfterEntities { context ->
        val pos = trackedPlayer?.pos ?: return@AfterEntities
        val world = MinecraftClient.getInstance().world ?: return@AfterEntities
        val camera = context.camera()
        if (!shouldDrawPlayerOutlineBox(world, camera.pos, pos)) return@AfterEntities


        val matrices = context.matrixStack()
        matrices?.push() ?: return@AfterEntities

        val vertexConsumers = context.consumers()


        val relativeX = pos.x - camera.pos.x
        val relativeY = pos.y - camera.pos.y
        val relativeZ = pos.z - camera.pos.z

        matrices.translate(relativeX, relativeY, relativeZ)

        val vertexConsumer = vertexConsumers?.getBuffer(XRAY_LINES_LAYER) ?: return@AfterEntities

        val box = Box(-0.5, 0.0, -0.5, 0.5, 1.8, 0.5)
        drawPlayerOutlineBox(matrices.peek().positionMatrix, vertexConsumer, box, 1.0f, 0.0f, 0.0f, 1.0f)

        matrices.pop()

    })

    private fun shouldDrawPlayerOutlineBox(world: World, cameraPos: Vec3d, targetPos: Vec3d): Boolean {
        val offsets = listOf(
            Vec3d(0.0, 1.0, 0.0),  // Head
            Vec3d(0.0, 0.5, 0.0),  // Chest
            Vec3d(0.3, 0.5, 0.0),  // Right shoulder
            Vec3d(-0.3, 0.5, 0.0), // Left shoulder
            Vec3d(0.0, 0.5, 0.3),  // Front
            Vec3d(0.0, 0.5, -0.3)  // Back
        )

        for (offset in offsets) {
            if (rayHitsSolidBlock(world, cameraPos, targetPos.add(offset))) {
                return true
            }
        }

        return false
    }

    private fun rayHitsSolidBlock(world: World, from: Vec3d, to: Vec3d): Boolean {
        val direction = to.subtract(from).normalize()
        val distance = from.distanceTo(to)

        val stepSize = 0.2
        var traveled = 0.0

        while (traveled < distance) {
            val currentPos = from.add(direction.multiply(traveled))
            val blockPos = BlockPos.ofFloored(currentPos)

            val blockState = world.getBlockState(blockPos)
            if (!blockState.isAir && !blockState.isTransparent) {
                return true
            }
            traveled += stepSize
        }

        return false
    }

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


    val XRAY_LINES_PIPELINE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(
            RenderPipelines.RENDERTYPE_LINES_SNIPPET
        )
            .withLocation("pipeline/xray_lines")
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
            .withUniform("LineWidth", UniformType.FLOAT)
            .withUniform("ScreenSize", UniformType.VEC2)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, DrawMode.LINES)
            .build()
    )

    private val XRAY_LINES_LAYER = RenderLayer.of(
        "player_xray_outlines",
        1536,
        XRAY_LINES_PIPELINE,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(LineWidth(OptionalDouble.of(2.0)))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .build(false)
    )


}