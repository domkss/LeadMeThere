import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class ToggleButtonWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val textRenderer: TextRenderer,
    private val playerName: String,
    private val onToggle: (String, Boolean) -> Unit
) : ClickableWidget(x, y, width, height, Text.of(playerName)) {

    var isToggled = false
        private set

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val backgroundColor = if (isToggled) 0xFF00AA00.toInt() else 0xFFAA0000.toInt()
        context.fill(x, y, x + width, y + height, backgroundColor)

        val textColor = 0xFFFFFFFF.toInt()
        context.drawCenteredTextWithShadow(textRenderer, Text.of(playerName), x + width / 2, y + (height - 8) / 2, textColor)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        toggle()
    }

    private fun toggle() {
        isToggled = !isToggled
        onToggle(playerName, isToggled)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
    }
}
