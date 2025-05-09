/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere.gui.elements

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class RadioButtonWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val textRenderer: TextRenderer,
    private val playerName: String,
    private val onSelected: (RadioButtonWidget) -> Unit
) : ClickableWidget(x, y, width, height, Text.of(playerName)) {

    var isToggled = false
        private set

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val backgroundColor = if (isToggled) 0xFF00AA00.toInt() else 0xFFAA0000.toInt()
        context.fill(x, y, x + width, y + height, backgroundColor)

        val textColor = 0xFFFFFFFF.toInt()
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.of(playerName),
            x + width / 2,
            y + (height - 8) / 2,
            textColor
        )
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        onSelected(this)  // Tell the parent "I was clicked"
    }

    fun setToggled(value: Boolean) {
        isToggled = value
    }

    fun getButtonText(): String {
        return playerName
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
    }
}
