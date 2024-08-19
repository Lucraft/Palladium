package net.threetag.palladium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
//import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.model.animation.PalladiumAnimationRegistry;
import net.threetag.palladium.entity.BodyPart;
import net.threetag.palladium.entity.PlayerModelCacheExtension;
import net.threetag.palladium.mixin.client.AgeableListModelInvoker;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.RenderUtil;
import org.joml.Vector3f;

import java.util.Objects;

@SuppressWarnings({"rawtypes"})
public class HumanoidRendererModifications {

    public static float ALPHA_MULTIPLIER = 1F;
    private static float CACHED_SHRINK = 0F;
    public static BodyPart.ModifiedBodyPartResult CACHED_HIDE_RESULT = null;
    public static float CACHED_LIMB_SWING, CACHED_LIMB_SWING_AMOUNT, CACHED_AGE_IN_TICKS, CACHED_NET_HEAD_YAW, CACHED_HEAD_PITCH;

    public static void preSetup(LivingEntityRenderer renderer, LivingEntity entity, HumanoidModel model, PoseStack poseStack, float partialTicks) {
        // reset poses
        if (model instanceof AgeableListModelInvoker invoker) {
            PalladiumAnimationRegistry.resetPoses(invoker.invokeHeadParts(), invoker.invokeBodyParts());
        }

        // rotate player model
        if (renderer instanceof PlayerRenderer playerRenderer && entity instanceof AbstractClientPlayer player) {
            PalladiumAnimationRegistry.setupRotations(playerRenderer, player, poseStack, partialTicks);
        }
    }

    public static void preRender(LivingEntityRenderer renderer, LivingEntity entity, HumanoidModel model, PoseStack poseStack, float partialTick) {
        // animations
        if (!PalladiumAnimationRegistry.SKIP_ANIMATIONS) {
            PalladiumAnimationRegistry.applyAnimations(model, entity, CACHED_LIMB_SWING, CACHED_LIMB_SWING_AMOUNT, CACHED_AGE_IN_TICKS, CACHED_NET_HEAD_YAW, CACHED_HEAD_PITCH);
        }

        // visibility
//        if (!FirstPersonMode.isFirstPersonPass()) {
//            BodyPart.resetBodyParts(entity, model);
//        }
        CACHED_HIDE_RESULT = BodyPart.getModifiedBodyParts(entity, false);
        BodyPart.hideHiddenOrRemovedParts(model, entity, CACHED_HIDE_RESULT);

        // layer shrinking
        float scale = AbilityUtil.getHighestAnimationTimerProgress(entity, AbilitySerializers.SHRINK_BODY_OVERLAY.value(), partialTick, Easing.INOUTSINE);

        if (scale != 0F) {
            float f = -0.11F * scale;
            CACHED_SHRINK = f;
            Vector3f vec = new Vector3f(f, f, f);
            for (BodyPart value : BodyPart.values()) {
                ModelPart part = value.getModelPart(model);
                if (value.isOverlay() && part != null) {
                    part.offsetScale(vec);
                }
            }
        }
    }

    public static void preLayers(LivingEntityRenderer renderer, LivingEntity entity, HumanoidModel model, PoseStack poseStack) {
        // reset layer-shrink
        if (CACHED_SHRINK != 0F) {
            float f = -CACHED_SHRINK;
            CACHED_SHRINK = 0F;
            Vector3f vec = new Vector3f(f, f, f);
            for (BodyPart value : BodyPart.values()) {
                ModelPart part = value.getModelPart(model);
                if (value.isOverlay() && part != null) {
                    part.offsetScale(vec);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void postLayers(LivingEntityRenderer renderer, LivingEntity entity, HumanoidModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
        float vibrate = AbilityUtil.getHighestAnimationTimerProgress(entity, AbilitySerializers.VIBRATE.value(), partialTick, Easing.INOUTSINE);

        if (vibrate > 0F) {
            ALPHA_MULTIPLIER = 0.3F;
            var minecraft = Minecraft.getInstance();
            boolean bl = renderer.isBodyVisible(entity);
            boolean bl2 = !bl && !entity.isInvisibleTo(Objects.requireNonNull(minecraft.player));
            boolean bl3 = minecraft.shouldEntityAppearGlowing(entity);
            RenderType renderType = renderer.getRenderType(entity, bl, true, bl3);
            for (int i = 0; i < 10; i++) {
                poseStack.pushPose();

                var rand = RandomSource.create();
                poseStack.translate((rand.nextFloat() - 0.5F) / 15 * vibrate, 0, (rand.nextFloat() - 0.5F) / 15 * vibrate);

                if (renderType != null) {
                    VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
                    int m = LivingEntityRenderer.getOverlayCoords(entity, renderer.getWhiteOverlayProgress(entity, partialTick));
                    renderer.getModel().renderToBuffer(poseStack, vertexConsumer, packedLight, m, bl2 ? RenderUtil.rgbaToInt(1F, 1F, 1F, 0.15F) : -1);
                }

                poseStack.popPose();
                poseStack.pushPose();

                poseStack.translate((rand.nextFloat() - 0.5F) / 7 * vibrate, 0, (rand.nextFloat() - 0.5F) / 7 * vibrate);

                if (!entity.isSpectator()) {
                    for (Object layer : renderer.layers) {
                        if (layer instanceof RenderLayer renderLayer) {
                            renderLayer.render(poseStack, buffer, packedLight, entity, CACHED_LIMB_SWING, CACHED_LIMB_SWING_AMOUNT, partialTick, CACHED_AGE_IN_TICKS, CACHED_NET_HEAD_YAW, CACHED_HEAD_PITCH);
                        }
                    }
                }
                poseStack.popPose();
            }
            ALPHA_MULTIPLIER = 1F;
        }

        if (entity instanceof PlayerModelCacheExtension ext) {
            for (BodyPart part : BodyPart.values()) {
                var orig = part.getModelPart(model);
                var cache = part.getModelPart(ext.palladium$getCachedModel());

                if (orig != null && cache != null) {
                    cache.loadPose(orig.storePose());
                }
            }
        }
    }

    public static void applyRemovedBodyParts(HumanoidModel model) {
        var hideResult = HumanoidRendererModifications.CACHED_HIDE_RESULT;

        if (hideResult == null)
            return;

        if (hideResult.isRemoved(BodyPart.HEAD))
            model.head.visible = model.hat.visible = false;
        if (hideResult.isRemoved(BodyPart.CHEST))
            model.body.visible = false;
        if (hideResult.isRemoved(BodyPart.RIGHT_ARM))
            model.rightArm.visible = false;
        if (hideResult.isRemoved(BodyPart.LEFT_ARM))
            model.leftArm.visible = false;
        if (hideResult.isRemoved(BodyPart.RIGHT_LEG))
            model.rightLeg.visible = false;
        if (hideResult.isRemoved(BodyPart.LEFT_LEG))
            model.leftLeg.visible = false;
    }

}
