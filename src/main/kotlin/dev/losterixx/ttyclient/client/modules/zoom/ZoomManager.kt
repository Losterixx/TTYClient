package dev.losterixx.ttyclient.client.modules.zoom

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.ZoomConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import kotlin.math.abs

object ZoomManager : ClientModule {

    override val id = "zoom"
    override val configPath = "config/modules/zoom.jsonc"

    private var isZooming = false
    private var targetZoomLevel = 1.0f
    private var currentZoomLevel = 1.0f

    var config: ZoomConfig = ZoomConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            ZoomConfig::class.java
        ) { ZoomConfig() }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (isZooming && client.screen != null) {
                stopZoom()
            }
        }
    }

    fun trigger() {
        if (isZooming) stopZoom() else startZoom()
    }

    fun startZoom() {
        if (!config.enabled) return
        if (FreelookManager.isActive()) return

        isZooming = true
        targetZoomLevel = config.zoomStrength

        if (!config.zoomAnimation) {
            currentZoomLevel = targetZoomLevel
        }
    }

    fun stopZoom() {
        if (!isZooming) return

        isZooming = false
        targetZoomLevel = 1.0f

        if (!config.zoomAnimation) {
            currentZoomLevel = 1.0f
        }
    }

    fun updateZoomAnimation() {
        if (!config.zoomAnimation) {
            currentZoomLevel = targetZoomLevel
            return
        }

        val speed = config.zoomAnimationSpeed
        currentZoomLevel = lerp(currentZoomLevel, targetZoomLevel, speed)

        if (abs(currentZoomLevel - targetZoomLevel) < 0.001f) {
            currentZoomLevel = targetZoomLevel
        }
    }

    fun handleScroll(amount: Double): Boolean {
        if (!isZooming || !config.scrollable) return false

        val dynamicSensitivity = config.scrollStep * currentZoomLevel.coerceAtLeast(0.2f)
        val newZoom = (currentZoomLevel - amount.toFloat() * dynamicSensitivity).coerceIn(0.01f, 1.0f)

        currentZoomLevel = newZoom
        targetZoomLevel = newZoom
        return true
    }

    fun getFOVMultiplier(): Float = currentZoomLevel

    fun isZooming(): Boolean = isZooming || currentZoomLevel < 0.99f

    fun isActive(): Boolean = isZooming

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}