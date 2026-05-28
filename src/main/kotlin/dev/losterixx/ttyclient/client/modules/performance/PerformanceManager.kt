package dev.losterixx.ttyclient.client.modules.performance

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.PerformanceConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory

object PerformanceManager : ClientModule {

    override val id = "performance"
    override val configPath = "config/modules/performance.jsonc"
    override val displayName = "Performance"
    override val description = "Collection of client-side performance optimisations (HUD throttle, ...)."
    override val category = ModuleCategory.RENDER

    var config: PerformanceConfig = PerformanceConfig()
        private set

    @Volatile var isThrottleActive: Boolean = false
    @Volatile var lastExtractionNs: Long = 0L

    private var hudCaptureTarget: RenderTarget? = null

    fun ensureHudCaptureTarget(width: Int, height: Int): RenderTarget {
        val existing = hudCaptureTarget

        if (existing != null && existing.width == width && existing.height == height) return existing

        existing?.destroyBuffers()

        return TextureTarget("HudCapture", width, height, true).also { hudCaptureTarget = it }
    }

    fun getHudCaptureTargetOrNull(): RenderTarget? = hudCaptureTarget

    override fun load() {
        config = ConfigManager.loadConfig(configPath, PerformanceConfig::class.java) { PerformanceConfig() }
    }

    fun shouldSkip(): Boolean {
        if (!config.hudThrottleEnabled || config.hudThrottleIntervalMs <= 0L) {
            isThrottleActive = false
            return false
        }

        val nowNs = System.nanoTime()
        val elapsedMs = (nowNs - lastExtractionNs) / 1_000_000L

        return if (elapsedMs >= config.hudThrottleIntervalMs) {
            lastExtractionNs = nowNs
            isThrottleActive = false
            false
        } else {
            isThrottleActive = true
            true
        }
    }

    fun forceUpdate() { lastExtractionNs = 0L }
}
