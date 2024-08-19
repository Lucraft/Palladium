package net.threetag.palladium.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.accessory.Accessory;
import net.threetag.palladium.client.model.animation.PalladiumAnimationRegistry;
import net.threetag.palladium.client.renderer.item.armor.ArmorRendererManager;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;
import net.threetag.palladium.entity.BodyPart;
import net.threetag.palladium.entity.PalladiumPlayerExtension;
import net.threetag.palladium.entity.PlayerModelCacheExtension;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.RenderUtil;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    private float cachedHandShrink = 0F;
    private BodyPart.ModifiedBodyPartResult cachedHideResult = null;

    @Inject(at = @At("HEAD"), method = "renderHand")
    public void renderHandPre(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear, CallbackInfo ci) {
        PlayerRenderer playerRenderer = (PlayerRenderer) (Object) this;
        RenderUtil.REDIRECT_GET_BUFFER = true;
        PalladiumAnimationRegistry.SKIP_ANIMATIONS = true;

        PalladiumAnimationRegistry.applyFirstPersonAnimations(poseStack, player, playerRenderer.getModel(), rendererArm == playerRenderer.getModel().rightArm);

        if (playerRenderer.getModel() instanceof AgeableListModelInvoker invoker) {
            PalladiumAnimationRegistry.resetPoses(invoker.invokeHeadParts(), invoker.invokeBodyParts());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/PlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER), method = "renderHand")
    public void renderHandPreRender(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear, CallbackInfo ci) {
        PlayerRenderer playerRenderer = (PlayerRenderer) (Object) this;
        PalladiumAnimationRegistry.SKIP_ANIMATIONS = false;

        // Reset all, make them visible
        BodyPart.resetBodyParts(player, playerRenderer.getModel());

        // Make them invisible if specified
        this.cachedHideResult = BodyPart.getModifiedBodyParts(player, true);
        var bodyPart = rendererArm == playerRenderer.getModel().rightArm ? BodyPart.RIGHT_ARM : BodyPart.LEFT_ARM;
        var bodyPartOverlay = rendererArm == playerRenderer.getModel().rightArm ? BodyPart.RIGHT_ARM_OVERLAY : BodyPart.LEFT_ARM_OVERLAY;

        if (this.cachedHideResult.isHiddenOrRemoved(bodyPart)) {
            rendererArm.visible = false;
        }

        if (this.cachedHideResult.isHiddenOrRemoved(bodyPartOverlay)) {
            rendererArmwear.visible = false;
        }

        // Shrink Overlay
        float scale = AnimationTimer.getValue(player, AbilitySerializers.SHRINK_BODY_OVERLAY.get(), Minecraft.getInstance().getFrameTime(), Easing.INOUTSINE);

        if (scale != 0F) {
            float f = -0.11F * scale;
            this.cachedHandShrink = f;
            Vector3f vec = new Vector3f(f, f, f);
            rendererArmwear.offsetScale(vec);
        }
    }

    @Inject(at = @At("RETURN"), method = "renderHand")
    public void renderHandPost(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear, CallbackInfo ci) {
        PlayerRenderer playerRenderer = (PlayerRenderer) (Object) this;
        boolean rightArm = rendererArm == playerRenderer.getModel().rightArm;

        // Visibilities
        BodyPart.resetBodyParts(player, playerRenderer.getModel());
        BodyPart.hideRemovedParts(playerRenderer.getModel(), player, this.cachedHideResult);
        this.cachedHideResult = null;

        // Render accessories
        Accessory.getPlayerData(player).ifPresent(data -> data.getSlots().forEach((slot, accessories) -> {
            for (Accessory accessory : accessories) {
                var arm = rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
                if (accessory.isVisible(slot, player, true) && accessory.canRenderAsArm(slot, arm, player)) {
                    accessory.renderArm(arm, player, playerRenderer, rendererArm, rendererArmwear, slot, poseStack, buffer, combinedLight);
                }
            }
        }));

        // Armor model stuff
        ArmorRendererManager.renderFirstPerson(player, poseStack, buffer, combinedLight, rendererArm, rightArm);

        PackRenderLayerManager.forEachLayer(player, (context, layer) -> {
            layer.renderArm(context, rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT, playerRenderer, poseStack, buffer, combinedLight);
        });

        RenderUtil.REDIRECT_GET_BUFFER = false;

        // Reset overlay shrink
        if (this.cachedHandShrink != 0F) {
            float f = -this.cachedHandShrink;
            this.cachedHandShrink = 0F;
            Vector3f vec = new Vector3f(f, f, f);
            rendererArmwear.offsetScale(vec);
        }

        // Apply model animations in first person
        if (player instanceof PlayerModelCacheExtension ext) {
            float partialTick = Minecraft.getInstance().getFrameTime();
            float f = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            float g = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
            float h = g - f;
            float k = 0.0F;
            float l = 0.0F;
            float i;
            if (!player.isPassenger() && player.isAlive()) {
                k = player.walkAnimation.speed(partialTick);
                l = player.walkAnimation.position(partialTick);
                if (player.isBaby()) {
                    l *= 3.0F;
                }

                if (k > 1.0F) {
                    k = 1.0F;
                }
            }

            if (player.isPassenger() && player.getVehicle() instanceof LivingEntity livingEntity) {
                f = Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
                h = g - f;
                i = Mth.wrapDegrees(h);
                if (i < -85.0F) {
                    i = -85.0F;
                }

                if (i >= 85.0F) {
                    i = 85.0F;
                }

                f = g - i;
                if (i * i > 2500.0F) {
                    f += i * 0.2F;
                }

                h = g - f;
            }

            float j = Mth.lerp(partialTick, player.xRotO, player.getXRot());
            if (LivingEntityRenderer.isEntityUpsideDown(player)) {
                j *= -1.0F;
                h *= -1.0F;
            }

            ext.palladium$getCachedModel().prepareMobModel(player, l, k, partialTick);
            ext.palladium$getCachedModel().setupAnim(player, l, k, player.tickCount + partialTick, h, j);
            if (!PalladiumAnimationRegistry.SKIP_ANIMATIONS) {
                PalladiumAnimationRegistry.applyAnimations(ext.palladium$getCachedModel(), player, l, k, player.tickCount + partialTick, h, j);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "setModelProperties")
    private void setModelProperties(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        PlayerRenderer renderer = (PlayerRenderer) (Object) this;
        PlayerModel<AbstractClientPlayer> playerModel = renderer.getModel();

        if (playerModel.crouching && clientPlayer instanceof PalladiumPlayerExtension extension) {
            var flightHandler = extension.palladium$getFlightHandler();
            var hover = flightHandler.getHoveringAnimation(0);
            var levitation = flightHandler.getLevitationAnimation(0);
            var flight = flightHandler.getFlightAnimation(0);

            if (hover > 0F || levitation > 0F || flight > 0F) {
                playerModel.crouching = false;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;", cancellable = true)
    public void getRenderOffset(AbstractClientPlayer entity, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if (entity instanceof PalladiumPlayerExtension extension) {
            var flightHandler = extension.palladium$getFlightHandler();
            var hover = flightHandler.getHoveringAnimation(0);
            var levitation = flightHandler.getLevitationAnimation(0);
            var flight = flightHandler.getFlightAnimation(0);

            if (hover > 0F || levitation > 0F || flight > 0F) {
                cir.setReturnValue(Vec3.ZERO);
            }
        }
    }

}
