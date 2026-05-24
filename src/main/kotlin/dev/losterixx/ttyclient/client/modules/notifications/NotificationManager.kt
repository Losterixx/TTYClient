package dev.losterixx.ttyclient.client.modules.notifications

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.NotificationsConfig
import dev.losterixx.ttyclient.client.event.EventBus
import dev.losterixx.ttyclient.client.event.ModuleToggleEvent
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import dev.losterixx.ttyclient.client.ui.Draw
import dev.losterixx.ttyclient.client.ui.Theme
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow

enum class NotificationType(
    val accentColor: Int
) {
    INFO(0xFF5599FF.toInt()),
    SUCCESS(0xFF44CC66.toInt()),
    WARNING(0xFFFFAA00.toInt()),
    ERROR(0xFFEF4444.toInt()),
}

data class NotificationEntry(
    val id: Long,
    val title: String,
    val message: String,
    val type: NotificationType,
    val durationMs: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val silent: Boolean = false,
)

object NotificationManager : ClientModule {

    override val id = "notifications"
    override val configPath = "config/modules/notifications.jsonc"
    override val displayName = "Notifications"
    override val description = "Shows toast-style notifications in a configurable screen corner."
    override val category = ModuleCategory.VISUAL

    private const val ENTER_MS = 300L
    private const val EXIT_MS = 500L

    private val idCounter = AtomicLong(0)

    var config: NotificationsConfig = NotificationsConfig()
        private set

    private val pending = ConcurrentLinkedQueue<NotificationEntry>()
    private val active = mutableListOf<NotificationEntry>()

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            NotificationsConfig::class.java
        ) { NotificationsConfig() }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            val now = System.currentTimeMillis()

                while (pending.isNotEmpty()) {
                    val entry = pending.poll() ?: break
                    if (active.size >= config.maxVisible) active.removeFirstOrNull()
                    active.add(entry)
                    if (!entry.silent) playSound(entry.type)
                }

            active.removeAll { entry ->
                val age = now - entry.createdAt

                when (config.animation.uppercase()) {
                    "NONE" -> age >= entry.durationMs
                    else -> age >= entry.durationMs + EXIT_MS + 200L
                }
            }
        }

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("ttyclient", "notifications"),
            HudElement { ctx, _ ->
                if (!config.enabled) return@HudElement
                if (active.isEmpty()) return@HudElement
                render(ctx)
            }
        )

        EventBus.on<ModuleToggleEvent> { event ->
            if (!config.enabled) return@on
            val action = if (event.enabled) "Module has been enabled." else "Module has been disabled."
            val type = if (event.enabled) NotificationType.SUCCESS else NotificationType.INFO
            show(event.module.displayName, action, type, 2500L)
        }
    }

    @JvmStatic
    fun show(
        title: String,
        message: String = "",
        type: NotificationType = NotificationType.INFO,
        durationMs: Long = -1L,
        silent: Boolean = false,
    ) {
        if (!config.enabled) return
        val duration = if (durationMs < 0L) config.durationMs else durationMs
        pending.add(NotificationEntry(idCounter.getAndIncrement(), title, message, type, duration, silent = silent))
    }

    private fun render(ctx: net.minecraft.client.gui.GuiGraphicsExtractor) {
        val mc = Minecraft.getInstance()
        val sw = mc.window.guiScaledWidth
        val sh = mc.window.guiScaledHeight
        val now = System.currentTimeMillis()

        val fh = Draw.fontHeight
        val padV = 5
        val padVBot = 4
        val padH = 6
        val bw = 3
        val barH = 1
        val innerH = fh + 2 + fh
        val notifH = padV + innerH + padVBot + barH

        val isRight = config.position.contains("RIGHT", ignoreCase = true)
        val isBottom = config.position.contains("BOTTOM", ignoreCase = true)
        val baseX = if (isRight) sw - config.marginX - config.width else config.marginX

        active.toList().forEachIndexed { index, notif ->
            val elapsed = now - notif.createdAt

            val alpha: Float = when (config.animation.uppercase()) {
                "NONE" -> if (elapsed < notif.durationMs) 1f else 0f
                else -> {
                    val fadeIn = if (elapsed < ENTER_MS)
                        (elapsed.toFloat() / ENTER_MS).coerceIn(0f, 1f)
                    else 1f

                    val fadeOut = when {
                        elapsed < notif.durationMs -> 1f
                        else -> {
                            val over = elapsed - notif.durationMs
                            if (over < EXIT_MS) (1f - over.toFloat() / EXIT_MS).coerceAtLeast(0f) else 0f
                        }
                    }

                    (fadeIn * fadeOut).coerceIn(0f, 1f)
                }
            }

            if (alpha <= 0f) return@forEachIndexed

            val slideOffset: Float = when (config.animation.uppercase()) {
                "SLIDE" -> if (elapsed < ENTER_MS) {
                    val t = elapsed.toFloat() / ENTER_MS
                    val eased = 1f - (1f - t).pow(3f)
                    val dist = config.width * (1f - eased)
                    if (isRight) dist else -dist
                } else 0f

                else -> 0f
            }

            val stackOff = index * (notifH + config.gap)
            val drawY = if (isBottom) sh - config.marginY - notifH - stackOff
                        else config.marginY + stackOff
            val drawX = (baseX + slideOffset).toInt()

            val a = (alpha * 255).toInt().coerceIn(0, 255)

            Draw.rect(ctx, drawX, drawY, config.width, notifH, Draw.withAlpha(Theme.bgPrimary, (a * 0.93f).toInt()))
            Draw.rect(ctx, drawX, drawY, bw, notifH, Draw.withAlpha(notif.type.accentColor, a))

            val textX = drawX + bw + padH
            val titleY = drawY + padV
            Draw.text(ctx, notif.title, textX, titleY, Draw.withAlpha(Theme.textPrimary, a), shadow = true)

            if (notif.message.isNotEmpty()) {
                Draw.text(ctx, notif.message, textX, titleY + fh + 2, Draw.withAlpha(Theme.textSecondary, a))
            }

            val progress = (elapsed.toFloat() / notif.durationMs).coerceIn(0f, 1f)
            val barY = drawY + notifH - barH
            val barX = drawX + bw
            val barW = config.width - bw
            Draw.rect(ctx, barX, barY, barW, barH, Draw.withAlpha(Theme.bgTertiary, (a * 0.5f).toInt()))
            Draw.rect(ctx, barX, barY, ((1f - progress) * barW).toInt(), barH, Draw.withAlpha(notif.type.accentColor, (a * 0.9f).toInt()))
        }
    }

    private fun playSound(type: NotificationType) {
        if (!config.sound) return

        val pitch = when (type) {
            NotificationType.INFO -> 1.0f
            NotificationType.SUCCESS -> 1.4f
            NotificationType.WARNING -> 0.75f
            NotificationType.ERROR -> 0.5f
        }

        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, pitch))
    }
}
