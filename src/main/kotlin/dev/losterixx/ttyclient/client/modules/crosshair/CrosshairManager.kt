package dev.losterixx.ttyclient.client.modules.crosshair

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.CrosshairConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.AttackIndicatorStatus
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity

object CrosshairManager : ClientModule {

    override val id = "crosshair"
    override val configPath = "config/modules/crosshair.jsonc"
    override val displayName = "Custom Crosshair"
    override val description = "Replaces the vanilla crosshair with a fully configurable pixel grid."
    override val category = ModuleCategory.VISUAL

    private val INVERT_PIPELINE: RenderPipeline by lazy {
        RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withColorTargetState(ColorTargetState(BlendFunction.INVERT))
            .build()
    }

    var config: CrosshairConfig = CrosshairConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            CrosshairConfig::class.java,
            useThemeGson = true
        ) { CrosshairConfig() }
    }

    override fun register() {
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR) { vanilla ->
            HudElement { ctx, deltaTracker ->
                val mc = Minecraft.getInstance()

                if (!config.enabled
                    || mc.options.hideGui
                    || mc.options.cameraType != CameraType.FIRST_PERSON
                    || mc.debugEntries.isOverlayVisible
                ) {
                    vanilla.extractRenderState(ctx, deltaTracker)
                    return@HudElement
                }

                val grid = config.grid.map { it.replace(" ", "") }

                if (grid.size != 15 || grid.any { it.length != 15 }) {
                    vanilla.extractRenderState(ctx, deltaTracker)
                    return@HudElement
                }

                val w = ctx.guiWidth()
                val h = ctx.guiHeight()
                val cx = w / 2
                val cy = h / 2

                val color = config.color

                val startX = cx - 7
                val startY = cy - 7

                for (row in 0 until 15) {
                    val rowStr = grid[row]

                    for (col in 0 until 15) {
                        if (rowStr[col] == 'X') {
                            val x1 = startX + col
                            val y1 = startY + row

                            if (color == null) {
                                ctx.fill(INVERT_PIPELINE, x1, y1, x1 + 1, y1 + 1, -1)
                            } else {
                                ctx.fill(x1, y1, x1 + 1, y1 + 1, color)
                            }
                        }
                    }
                }

                if (config.showAttackIndicator && mc.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                    val player = mc.player ?: return@HudElement
                    val cooldown = player.getAttackStrengthScale(0.0f)
                    var isFullCharge = false

                    val target = mc.crosshairPickEntity
                    if (target != null && target is LivingEntity && cooldown >= 1.0f) {
                        isFullCharge = player.currentItemAttackStrengthDelay > 5.0f && target.isAlive
                    }

                    val indicatorX = cx - 8
                    val indicatorY = cy - 7 + 16

                    if (isFullCharge) {
                        ctx.blitSprite(
                            RenderPipelines.CROSSHAIR,
                            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_full"),
                            indicatorX, indicatorY, 16, 16
                        )
                    } else if (cooldown < 1.0f) {
                        val progress = (cooldown * 17.0f).toInt()

                        ctx.blitSprite(
                            RenderPipelines.CROSSHAIR,
                            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background"),
                            indicatorX, indicatorY, 16, 4
                        )

                        ctx.blitSprite(
                            RenderPipelines.CROSSHAIR,
                            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress"),
                            16, 4, 0, 0,
                            indicatorX, indicatorY, progress, 4
                        )
                    }
                }
            }
        }
    }
}




