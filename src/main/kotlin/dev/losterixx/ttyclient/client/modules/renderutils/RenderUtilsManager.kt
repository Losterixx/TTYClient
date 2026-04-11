package dev.losterixx.ttyclient.client.modules.renderutils

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.RenderUtilsConfig
import dev.losterixx.ttyclient.client.modules.ClientModule

object RenderUtilsManager : ClientModule {

    override val id = "renderutils"
    override val configPath = "config/modules/renderutils.jsonc"
    override val displayName = "Render Utils"
    override val description = "Tweaks for shield/totem transforms and the fire overlay height."
    override val category = dev.losterixx.ttyclient.client.modules.ModuleCategory.RENDER

    var config: RenderUtilsConfig = RenderUtilsConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            RenderUtilsConfig::class.java
        ) { RenderUtilsConfig() }
    }
}

