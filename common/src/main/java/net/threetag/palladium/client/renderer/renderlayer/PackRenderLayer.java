package net.threetag.palladium.client.renderer.renderlayer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.dynamictexture.DynamicModelLayerLocation;
import net.threetag.palladium.client.dynamictexture.DynamicTexture;
import net.threetag.palladium.client.dynamictexture.DynamicTextureManager;
import net.threetag.palladium.client.model.ExtraAnimatedModel;
import net.threetag.palladium.util.SkinTypedValue;
import net.threetag.palladium.util.context.DataContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PackRenderLayer extends AbstractPackRenderLayer {

    private final SkinTypedValue<ModelTypes.Model> modelLookup;
    private final SkinTypedValue<ModelCache> model;
    private final SkinTypedValue<DynamicTexture> texture;
    private final RenderTypeFunction renderType;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PackRenderLayer(SkinTypedValue<ModelTypes.Model> model, SkinTypedValue<DynamicModelLayerLocation> modelLayerLocation, SkinTypedValue<DynamicTexture> texture, RenderTypeFunction renderType) {
        this.modelLookup = model;
        this.model = new SkinTypedValue(new ModelCache(modelLayerLocation.getNormal()), new ModelCache(modelLayerLocation.getSlim()));
        this.texture = texture;
        this.renderType = renderType;
    }

    @Override
    public void render(DataContext context, PoseStack poseStack, MultiBufferSource bufferSource, EntityModel<Entity> parentModel, int packedLight, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = context.getEntity();
        if (IPackRenderLayer.conditionsFulfilled(entity, this.conditions, this.thirdPersonConditions) && this.modelLookup.get(entity).fitsEntity(entity, parentModel)) {
            EntityModel<?> entityModel = this.model.get(entity).getModel(context, this.modelLookup.get(entity));

            if (entityModel instanceof HumanoidModel entityHumanoidModel && parentModel instanceof HumanoidModel parentHumanoid) {
                IPackRenderLayer.copyModelProperties(entity, parentHumanoid, entityHumanoidModel);
            }

            if (entityModel instanceof ExtraAnimatedModel extra) {
                extra.extraAnimations(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks);
            }

            VertexConsumer vertexConsumer = this.renderType.createVertexConsumer(bufferSource, this.texture.get(entity).getTexture(context), context.getItem().hasFoil());

            entityModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void renderArm(DataContext context, HumanoidArm arm, PlayerRenderer playerRenderer, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var player = context.getEntity();
        if (IPackRenderLayer.conditionsFulfilled(player, this.conditions, this.firstPersonConditions) && this.modelLookup.get(player).fitsEntity(player, playerRenderer.getModel())) {
            EntityModel<?> entityModel = this.model.get(player).getModel(context, this.modelLookup.get(player));

            if (entityModel instanceof HumanoidModel humanoidModel) {
                playerRenderer.getModel().copyPropertiesTo(humanoidModel);
                VertexConsumer vertexConsumer = this.renderType.createVertexConsumer(bufferSource, this.texture.get(player).getTexture(context), context.getItem().hasFoil());

                humanoidModel.attackTime = 0.0F;
                humanoidModel.crouching = false;
                humanoidModel.swimAmount = 0.0F;

                if (arm == HumanoidArm.RIGHT) {
                    humanoidModel.rightArm.xRot = 0.0F;
                    humanoidModel.rightArm.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                } else {
                    humanoidModel.leftArm.xRot = 0.0F;
                    humanoidModel.leftArm.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
                }
            }
        }
    }

    @Override
    public void createSnapshot(DataContext context, EntityModel<Entity> parentModel, Consumer<Snapshot> consumer) {
        var entity = context.getEntity();

        if (IPackRenderLayer.conditionsFulfilled(entity, this.conditions, this.firstPersonConditions) && this.modelLookup.get(entity).fitsEntity(entity, parentModel)) {
            EntityModel<?> entityModel = this.model.get(entity).getModel(context, this.modelLookup.get(entity));
            var texture = this.texture.get(entity).getTexture(context);
            consumer.accept(new Snapshot(entityModel, texture));
        }
    }

    public static PackRenderLayer parse(JsonObject json) {
        var renderType = PackRenderLayerManager.getRenderType(new ResourceLocation(GsonHelper.getAsString(json, "render_type", "solid")));

        SkinTypedValue<ModelTypes.Model> model;
        String modelTypeKey = "model_type";

        if (!json.has(modelTypeKey) && json.has("model")) {
            AddonPackLog.warning("Deprecated use of 'model' in render layer. Please switch to 'model_type'!");
            modelTypeKey = "model";
        }

        if (GsonHelper.isValidNode(json, modelTypeKey)) {
            model = SkinTypedValue.fromJSON(json.get(modelTypeKey), jsonElement -> {
                ResourceLocation modelId = new ResourceLocation(jsonElement.getAsString());
                ModelTypes.Model m = ModelTypes.get(modelId);

                if (m == null) {
                    throw new JsonParseException("Unknown model type '" + modelId + "'");
                }

                return m;
            });
        } else {
            model = new SkinTypedValue<>(ModelTypes.HUMANOID);
        }

        if (renderType == null) {
            throw new JsonParseException("Unknown render type '" + new ResourceLocation(GsonHelper.getAsString(json, "render_type", "solid")) + "'");
        }

        return new PackRenderLayer(model, SkinTypedValue.fromJSON(json.get("model_layer"), DynamicModelLayerLocation::fromJson), SkinTypedValue.fromJSON(json.get("texture"), DynamicTextureManager::fromJson), renderType);
    }

    public static class ModelCache {

        private final DynamicModelLayerLocation modelLayerLocation;
        private final Map<ModelLayerLocation, EntityModel<?>> models = new HashMap<>();

        public ModelCache(DynamicModelLayerLocation modelLayerLocation) {
            this.modelLayerLocation = modelLayerLocation;
        }

        public EntityModel<?> getModel(DataContext context, ModelTypes.Model modelType) {
            var modelLayer = this.modelLayerLocation.getModelLayer(context);

            if (this.models.containsKey(modelLayer)) {
                return this.models.get(modelLayer);
            } else {
                var model = modelType.getModel(Minecraft.getInstance().getEntityModels().bakeLayer(modelLayer));
                this.models.put(modelLayer, model);
                return model;
            }
        }
    }

}
