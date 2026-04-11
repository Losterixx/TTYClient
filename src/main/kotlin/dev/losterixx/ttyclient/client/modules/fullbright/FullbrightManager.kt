package dev.losterixx.ttyclient.client.modules.fullbright

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.FullbrightConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import dev.losterixx.ttyclient.mixin.accessors.OptionInstanceAccessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object FullbrightManager : ClientModule {

    override val id = "fullbright"
    override val configPath = "config/modules/fullbright.jsonc"
    override val displayName = "Fullbright"
    override val description = "Boosts brightness via gamma override or night-vision effect."
    override val category = ModuleCategory.VISUAL

    private val mc: Minecraft get() = Minecraft.getInstance()

    private var originalGamma: Double = 1.0
    private var lastLevel: Any? = null
    private var pendingGammaApply = false
    private var initialized = false

    var config: FullbrightConfig = FullbrightConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            FullbrightConfig::class.java
        ) { FullbrightConfig() }

        if (!initialized) {
            initialized = true

            if (config.active && config.mode == "gamma") {
                pendingGammaApply = true
            }
        }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (pendingGammaApply) {
                pendingGammaApply = false
                originalGamma = getGamma()
                setGamma(config.gamma.toDouble())
            }

            val currentLevel = client.level
            if (currentLevel != null && currentLevel !== lastLevel) {
                lastLevel = currentLevel

                if (config.active && config.mode == "nightvision") {
                    applyNightVision()
                }
            } else if (currentLevel == null && lastLevel != null) {
                lastLevel = null
            }
        }
    }

    override fun onDisable() {
        disable()
    }

    fun trigger() {
        if (!config.enabled) return
        if (config.active) deactivate() else activate()
        ConfigManager.saveConfig("config/modules/fullbright.jsonc", config)
    }

    private fun activate() {
        config.active = true

        when (config.mode) {
            "gamma" -> {
                originalGamma = getGamma()
                setGamma(config.gamma.toDouble())
            }

            "nightvision" -> applyNightVision()
        }
    }

    private fun deactivate() {
        config.active = false

        when (config.mode) {
            "gamma" -> setGamma(originalGamma)
            "nightvision" -> mc.player?.removeEffect(MobEffects.NIGHT_VISION)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setGamma(value: Double) {
        (mc.options.gamma() as OptionInstanceAccessor).setOptionValue(value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getGamma(): Double {
        return (mc.options.gamma() as OptionInstanceAccessor).getOptionValue() as Double
    }

    private fun applyNightVision() {
        mc.player?.addEffect(MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0, false, false, false))
    }

    fun disable() {
        if (config.active) {
            deactivate()
            ConfigManager.saveConfig("config/modules/fullbright.jsonc", config)
        }
    }

    fun isActive(): Boolean = config.active
}







