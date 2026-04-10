package dev.losterixx.ttyclient.mixin.modules.renderutils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.losterixx.ttyclient.client.config.configs.modules.HandModelConfig;
import dev.losterixx.ttyclient.client.modules.renderutils.RenderUtilsManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void onRenderArmWithItemHead(AbstractClientPlayer player,
            float partialTick, float pitch, InteractionHand hand,
            float swingProgress, ItemStack stack, float equipProgress,
            PoseStack poseStack, SubmitNodeCollector collector,
            int packedLight, CallbackInfo ci) {

        var cfg = RenderUtilsManager.INSTANCE.getConfig();
        if (!cfg.getEnabled()) return;

        if (stack.is(Items.SHIELD) && cfg.getShield().getInvisible()) {
            ci.cancel();
        } else if (stack.is(Items.TOTEM_OF_UNDYING) && cfg.getTotem().getInvisible() && hand == InteractionHand.OFF_HAND) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "renderArmWithItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
        )
    )
    private void redirectRenderItem(ItemInHandRenderer self,
            LivingEntity entity, ItemStack stack, ItemDisplayContext ctx,
            PoseStack poseStack, SubmitNodeCollector collector, int light) {

        var cfg = RenderUtilsManager.INSTANCE.getConfig();
        if (cfg.getEnabled()) {
            if (stack.is(Items.SHIELD) && cfg.getShield().getEnabled()) {
                ttyApplyTransform(poseStack, cfg.getShield().getModel());
            } else if (stack.is(Items.TOTEM_OF_UNDYING) && cfg.getTotem().getEnabled()) {
                ttyApplyTransform(poseStack, cfg.getTotem().getModel());
            }
        }

        self.renderItem(entity, stack, ctx, poseStack, collector, light);
    }

    @Unique
    private static void ttyApplyTransform(PoseStack poseStack, HandModelConfig model) {
        poseStack.translate(model.getPosX(), model.getPosY(), model.getPosZ());
        poseStack.mulPose(Axis.XP.rotationDegrees(model.getRotX()));
        poseStack.mulPose(Axis.YP.rotationDegrees(model.getRotY()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(model.getRotZ()));
        poseStack.scale(model.getScaleX(), model.getScaleY(), model.getScaleZ());
    }
}
