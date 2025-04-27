/*
 * Copyright (c) 2025 Dominik Kiss
 * Repository: https://github.com/domkss/LeadMeThere
 *
 * This code is licensed under the MIT License.
 * See the attached LICENSE file for more information.
 */

package dev.domkss.leadmethere.gui

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW


object OpenTargetPlayerSelectorKey {
    private var openPlayerListKey: KeyBinding? = null

    fun register() {
        openPlayerListKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.leadmethere.open_player_list",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,  // Default key (Tab)
                "category.leadmethere.keys"
            )
        )
    }

    fun getKeyBinding(): KeyBinding? {
        return openPlayerListKey
    }


}