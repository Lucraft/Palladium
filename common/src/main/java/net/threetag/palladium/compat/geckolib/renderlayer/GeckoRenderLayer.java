package net.threetag.palladium.compat.geckolib.renderlayer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.dynamictexture.DynamicTexture;
import net.threetag.palladium.client.dynamictexture.DynamicTextureManager;
import net.threetag.palladium.client.renderer.renderlayer.*;
import net.threetag.palladium.compat.geckolib.playeranimator.ParsedAnimationController;
import net.threetag.palladium.entity.PalladiumLivingEntityExtension;
import net.threetag.palladium.util.RenderUtil;
import net.threetag.palladium.util.SkinTypedValue;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.json.GsonUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.Color;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes"})
public class GeckoRenderLayer extends AbstractPackRenderLayer {

    private final SkinTypedValue<DynamicTexture> texture;
    private final SkinTypedValue<DynamicTexture> modelLocation;
    public final ResourceLocation animationLocation;
    public final List<ParsedAnimationController<GeckoLayerState>> animationControllers;
    public ResourceLocation cachedTexture;
    public ResourceLocation cachedModel;
    public final RenderTypeFunction renderType;
    private final GeckoRenderLayerModel model;

    public GeckoRenderLayer(SkinTypedValue<DynamicTexture> texture, SkinTypedValue<DynamicTexture> modelLocation, ResourceLocation animationLocation, List<ParsedAnimationController<GeckoLayerState>> animationControllers, RenderTypeFunction renderType) {
        this.texture = texture;
        this.renderType = renderType;
        this.modelLocation = modelLocation;
        this.animationLocation = animationLocation;
        this.animationControllers = animationControllers;
        this.model = new GeckoRenderLayerModel(this);
    }

    public GeckoRenderLayerModel getModel() {
        return model;
    }

    @Nullable
    public GeckoLayerState getState(LivingEntity entity) {
        if (entity instanceof PalladiumLivingEntityExtension extension) {
            return extension.palladium$getRenderLayerStates().getOrCreate(this) instanceof GeckoLayerState state ? state : null;
        }
        return null;
    }

    @Override
    public RenderLayerStates.State createState() {
        return new GeckoLayerState(this);
    }

    @Override
    public void render(DataContext context, PoseStack poseStack, MultiBufferSource bufferSource, EntityModel<Entity> parentModel, int packedLight, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var living = context.getLivingEntity();

        if (living != null && IPackRenderLayer.conditionsFulfilled(living, this.conditions, this.thirdPersonConditions) && parentModel instanceof HumanoidModel parentHumanoid) {
            this.model.setCurrentRenderingFields(getState(living), living, parentHumanoid);
            this.model.setAllVisible(true);
            this.cachedTexture = this.texture.get(living).getTexture(context);
            this.cachedModel = this.modelLocation.get(living).getTexture(context);
            this.model.renderToBuffer(poseStack, this.renderType.createVertexConsumer(bufferSource, this.cachedTexture, false), packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void renderArm(DataContext context, HumanoidArm arm, PlayerRenderer playerRenderer, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var living = context.getLivingEntity();
        if (living != null && IPackRenderLayer.conditionsFulfilled(living, this.conditions, this.firstPersonConditions)) {
            var state = getState(living);
            this.model.setCurrentRenderingFields(state, living, playerRenderer.getModel());
            this.cachedTexture = this.texture.get(living).getTexture(context);
            this.cachedModel = this.modelLocation.get(living).getTexture(context);
            this.model.grabRelevantBones(this.model.getGeoModel().getBakedModel(this.model.getGeoModel().getModelResource(this.model.currentState)));

            var bone = (arm == HumanoidArm.RIGHT ? this.model.getRightArmBone() : this.model.getLeftArmBone());

            if (state != null && bone != null) {
                playerRenderer.getModel().copyPropertiesTo(this.model);
                this.model.applyBaseTransformations(playerRenderer.getModel());

                var partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                VertexConsumer buffer = state.layer.renderType.createVertexConsumer(bufferSource, this.model.getTextureLocation(state), false);

                poseStack.pushPose();
                poseStack.translate(0, 24 / 16F, 0);
                poseStack.scale(-1, -1, 1);

                Color renderColor = this.model.getRenderColor(state, partialTick, packedLight);
                float red = renderColor.getRedFloat();
                float green = renderColor.getGreenFloat();
                float blue = renderColor.getBlueFloat();
                float alpha = renderColor.getAlphaFloat();
                int packedOverlay = this.model.getPackedOverlay(state, 0, partialTick);

                AnimationState<GeckoLayerState> animationState = new AnimationState<>(state, 0, 0, partialTick, false);
                long instanceId = this.model.getInstanceId(state);

                animationState.setData(DataTickets.TICK, state.getTick(living));
                animationState.setData(DataTickets.ENTITY, living);
                animationState.setData(DataTickets.EQUIPMENT_SLOT, EquipmentSlot.CHEST);
                this.model.getGeoModel().addAdditionalStateData(state, instanceId, animationState::setData);
                this.model.getGeoModel().handleAnimations(state, instanceId, animationState, partialTick);
                this.model.renderRecursively(poseStack, state, bone, null, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, RenderUtil.rgbaToInt(red, green, blue, alpha));

                poseStack.popPose();
            }
        }
    }

    public static GeckoRenderLayer parse(JsonObject json) {
        SkinTypedValue<DynamicTexture> modelLocation = SkinTypedValue.fromJSON(json.get("model"), DynamicTextureManager::fromJson);
        var texture = SkinTypedValue.fromJSON(json.get("texture"), DynamicTextureManager::fromJson);
        var renderType = PackRenderLayerManager.getRenderType(GsonUtil.getAsResourceLocation(json, "render_type", ResourceLocation.withDefaultNamespace("solid")));

        if (renderType == null) {
            throw new JsonParseException("Unknown render type '" + GsonUtil.getAsResourceLocation(json, "render_type", ResourceLocation.withDefaultNamespace("solid")) + "'");
        }

        var layer = new GeckoRenderLayer(
                texture,
                modelLocation,
                GsonUtil.getAsResourceLocation(json, "animation_file", null),
                GsonUtil.fromListOrPrimitive(json.get("animation_controller"), el -> ParsedAnimationController.controllerFromJson(el.getAsJsonObject()), Collections.emptyList()),
                renderType
        );

        var bonesJson = GsonHelper.getAsJsonObject(json, "bones", new JsonObject());
        layer.model.headBone = GsonHelper.getAsString(bonesJson, "head", layer.model.headBone);
        layer.model.bodyBone = GsonHelper.getAsString(bonesJson, "body", layer.model.bodyBone);
        layer.model.rightArmBone = GsonHelper.getAsString(bonesJson, "right_arm", layer.model.rightArmBone);
        layer.model.leftArmBone = GsonHelper.getAsString(bonesJson, "left_arm", layer.model.leftArmBone);
        layer.model.rightLegBone = GsonHelper.getAsString(bonesJson, "right_leg", layer.model.rightLegBone);
        layer.model.leftLegBone = GsonHelper.getAsString(bonesJson, "left_leg", layer.model.leftLegBone);

        return IPackRenderLayer.parseConditions(layer, json);
    }

}
