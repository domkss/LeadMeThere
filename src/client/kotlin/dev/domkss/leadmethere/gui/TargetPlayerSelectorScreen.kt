package dev.domkss.leadmethere.gui

import PlayerListWidget
import RadioButtonWidget
import com.example.playerdirectionarrow.network.ObserverSubscribePayload
import com.example.playerdirectionarrow.network.ObserverUnsubscribePayload
import dev.domkss.leadmethere.LeadMeThereClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text


class PlayerListScreen(private val currentTargetPlayerName: String?) : Screen(Text.of("Select Player To Track")) {
    // Retrieve list of online players
    private var onlinePlayers: List<String> = arrayListOf()
    private var playerListWidget: PlayerListWidget? = null
    private val radioButtons = mutableListOf<RadioButtonWidget>()

    public override fun init() {
        val client = MinecraftClient.getInstance()
        val currentPlayerUuid = client.player?.uuid ?: return

        playerListWidget = PlayerListWidget(
            x = width / 2 - 100,  // Position X
            y = height / 6,       // Position Y
            width = 200,          // Width of the list
            height = 150          // Height (scrollable area)
        )


        //Create button for each online player
        onlinePlayers += "Turn Tracking Off"
        onlinePlayers += client.networkHandler?.playerList?.filter { !it.profile.id.equals(currentPlayerUuid) }
            ?.map { it.profile.name }
            ?.toList() ?: emptyList()


        for (player in onlinePlayers) {
            val firstListItem=(onlinePlayers.indexOf(player)==0)

            val radioButton = RadioButtonWidget(
                0, 0, 200, 20, textRenderer, player
            ) { clickedButton ->
                if(firstListItem){
                    val unSubscribePayload = ObserverUnsubscribePayload()
                    ClientPlayNetworking.send(unSubscribePayload)
                    LeadMeThereClient.trackedPlayer=null
                }else {
                    // Send the packet to the server
                    val selectedPlayerName = clickedButton.getButtonText()
                    val subscribePayload = ObserverSubscribePayload(selectedPlayerName)
                    ClientPlayNetworking.send(subscribePayload)
                }
                selectOnly(clickedButton)
            }
            if(currentTargetPlayerName==null&&firstListItem) radioButton.setToggled(true)
            else if (currentTargetPlayerName!=null && player.equals(currentTargetPlayerName, ignoreCase = true)) radioButton.setToggled(true)
            radioButtons.add(radioButton)
            playerListWidget!!.addEntry(radioButton)

        }

        addDrawableChild(playerListWidget)


    }


    private fun selectOnly(selectedButton: RadioButtonWidget) {
        for (button in radioButtons) {
            button.setToggled(button == selectedButton)
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)

        context.drawCenteredTextWithShadow(
            textRenderer,
            this.title,
            this.width / 2,
            20,
            0xFFFFFF // white color
        )
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (playerListWidget == null) return super.mouseClicked(mouseX, mouseY, button)
        else {
            return playerListWidget!!.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button)
        }
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        if (playerListWidget == null) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        else {
            return playerListWidget!!.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
            ) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        }
    }
}