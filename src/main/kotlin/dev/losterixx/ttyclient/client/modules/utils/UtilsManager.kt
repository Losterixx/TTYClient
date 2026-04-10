package dev.losterixx.ttyclient.client.modules.utils

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.UtilsConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand

object UtilsManager : ClientModule {

    override val id = "utils"
    override val configPath = "config/modules/utils.jsonc"

    var config: UtilsConfig = UtilsConfig()
        private set

    private var warnTick = 0
    private var blinkState = false

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            UtilsConfig::class.java
        ) { UtilsConfig() }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (config.enabled) checkDurability(mc)
        }
    }

    private fun checkDurability(mc: Minecraft) {
        val player = mc.player ?: return

        if (!config.durabilityWarning.enabled) {
            warnTick = 0
            return
        }

        val threshold = config.durabilityWarning.threshold / 100.0f

        val hasLow = listOf(
            player.getItemInHand(InteractionHand.MAIN_HAND),
            player.getItemInHand(InteractionHand.OFF_HAND)
        ).any { stack ->
            !stack.isEmpty && stack.isDamageableItem &&
            (1.0f - stack.damageValue.toFloat() / stack.maxDamage.toFloat()) < threshold
        }

        if (!hasLow) {
            warnTick = 0
            return
        }

        warnTick++

        if (warnTick % 10 == 1) {
            blinkState = !blinkState

            if (config.durabilityWarning.soundEnabled) {
                mc.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.2f))
            }
        }

        val color = if (blinkState) "§e" else "§c"
        player.sendOverlayMessage(Component.literal("${color}⚠ §lLow Durability §r${color}⚠"))
    }
}
