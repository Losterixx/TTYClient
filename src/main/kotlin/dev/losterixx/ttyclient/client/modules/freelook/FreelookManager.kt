
package dev.losterixx.ttyclient.client.modules.freelook

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.FreelookConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth

object FreelookManager : ClientModule {

    override val id = "freelook"
    override val configPath = "config/modules/freelook.jsonc"
    override val displayName = "Freelook"
    override val description = "Detaches the camera so you can look around freely without moving."
    override val category = ModuleCategory.VISUAL

    private val mc: Minecraft get() = Minecraft.getInstance()

    private var isFreelooking = false
    private var originalCameraType: CameraType? = null
    private var freelookYaw = 0f
    private var freelookPitch = 0f
    private var cameraDistance = 4.0f

    var config: FreelookConfig = FreelookConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            FreelookConfig::class.java
        ) { FreelookConfig() }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (isFreelooking && mc.screen != null) stopFreelook()
        }
    }

    fun trigger() {
        if (!config.enabled) return
        if (isFreelooking) stopFreelook() else startFreelook()
    }

    private fun startFreelook() {
        val player = mc.player ?: return

        isFreelooking = true
        originalCameraType = mc.options.cameraType

        if (mc.options.cameraType == CameraType.FIRST_PERSON) {
            mc.options.cameraType = CameraType.THIRD_PERSON_BACK
        }

        freelookYaw = player.yRot
        freelookPitch = player.xRot
    }

    private fun stopFreelook() {
        isFreelooking = false
        cameraDistance = 4.0f

        originalCameraType?.let {
            mc.options.cameraType = it
            originalCameraType = null
        }
    }

    override fun onDisable() {
        if (isFreelooking) stopFreelook()
    }

    fun isActive(): Boolean = isFreelooking

    fun getFreelookYaw(): Float = freelookYaw
    fun getFreelookPitch(): Float = freelookPitch
    fun getCameraDistance(): Float = cameraDistance

    fun handleScroll(delta: Double): Boolean {
        if (!isFreelooking || !config.scrollable) return false
        cameraDistance = (cameraDistance - delta.toFloat() * config.scrollStep).coerceIn(1.0f, config.maxDistance)
        return true
    }

    fun updateFreelookAngles(rawDX: Float, rawDY: Float) {
        if (!isFreelooking) return

        val yawChange = rawDX * config.sensitivity * if (config.invertYaw) -1 else 1
        val pitchChange = rawDY * config.sensitivity * if (config.invertPitch) -1 else 1

        freelookYaw = Mth.wrapDegrees(freelookYaw + yawChange)
        freelookPitch = (freelookPitch + pitchChange).coerceIn(-90f, 90f)
    }
}