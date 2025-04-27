/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere.gui.elements

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.util.math.MathHelper
import kotlin.math.max

class PlayerListWidget(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val itemHeight: Int = 24
) : net.minecraft.client.gui.AbstractParentElement(), net.minecraft.client.gui.Drawable,
    net.minecraft.client.gui.Selectable {

    private val children = mutableListOf<RadioButtonWidget>()
    private var scrollAmount = 0f

    fun addEntry(widget: RadioButtonWidget) {
        children.add(widget)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.enableScissor(x, y, x + width, y + height)

        val offsetY = -scrollAmount.toInt()
        for ((index, child) in children.withIndex()) {
            val childY = y + index * itemHeight + offsetY
            if (childY + itemHeight < y || childY > y + height) continue
            child.setPosition(x, childY)
            child.setWidth(width)
            child.render(context, mouseX, mouseY, delta)
        }

        context.disableScissor()
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        // Only scroll if the mouse is inside the list area
        if (mouseX in x.toDouble()..(x + width).toDouble() && mouseY in y.toDouble()..(y + height).toDouble()) {
            scrollAmount -= (verticalAmount * itemHeight / 2).toFloat()
            scrollAmount = MathHelper.clamp(scrollAmount, 0f, getMaxScroll())
            return true
        }
        return false
    }


    private fun getMaxScroll(): Float {
        val totalContentHeight = children.size * itemHeight
        return max(0f, (totalContentHeight - height).toFloat())
    }


    override fun children(): List<Element> = children


    override fun appendNarrations(builder: NarrationMessageBuilder?) {
        TODO("Not yet implemented")
    }

    override fun getType(): net.minecraft.client.gui.Selectable.SelectionType =
        net.minecraft.client.gui.Selectable.SelectionType.NONE
}
