package dev.losterixx.ttyclient.client.config.configs.modules

import dev.losterixx.ttyclient.client.config.Comment

data class HandModelConfig(
    var scaleX: Float = 1.0f,
    var scaleY: Float = 1.0f,
    var scaleZ: Float = 1.0f,
    var posX: Float = 0.0f,
    var posY: Float = 0.0f,
    var posZ: Float = 0.0f,
    var rotX: Float = 0.0f,
    var rotY: Float = 0.0f,
    var rotZ: Float = 0.0f,
)

data class ShieldRenderConfig(
    @Comment("Apply the custom model transform below to the shield.")
    var enabled: Boolean = false,

    @Comment("Hide the shield model entirely (in both hands).")
    var invisible: Boolean = false,

    @Comment("Custom transform for the shield item model in first person.")
    var model: HandModelConfig = HandModelConfig(posY = -0.25f),
)

data class TotemRenderConfig(
    @Comment("Apply the custom model transform below to the totem.")
    var enabled: Boolean = false,

    @Comment("Hide the totem in the off-hand (invisible totem).")
    var invisible: Boolean = false,

    @Comment("Custom transform for the totem item model in first person.",)
    var model: HandModelConfig = HandModelConfig(scaleX = 0.7f, scaleY = 0.7f, scaleZ = 0.7f),
)

data class FireRenderConfig(
    @Comment(
        "Vertical offset applied to the fire overlay.",
        "Negative = lower (less visible / low-fire). Positive = higher.",
        "Default is -0.25 (equivalent to 'low fire'). Set to 0.0 to disable."
    )

    var heightOffset: Float = -0.3f,
)

data class RenderUtilsConfig(
    @Comment("Enable or disable the RenderUtils module.", "Use ';module --toggle renderutils' to change this.")
    var enabled: Boolean = true,

    @Comment("Shield rendering settings")
    var shield: ShieldRenderConfig = ShieldRenderConfig(),

    @Comment("Totem of Undying rendering settings")
    var totem: TotemRenderConfig = TotemRenderConfig(),

    @Comment("Fire overlay rendering settings")
    var fire: FireRenderConfig = FireRenderConfig(),
)

