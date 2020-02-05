package net.threetag.threecore.client.renderer.entity.modellayer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.threetag.threecore.ThreeCore;
import net.threetag.threecore.ability.Ability;
import net.threetag.threecore.ability.AbilityHelper;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = ThreeCore.MODID, value = Dist.CLIENT)
public class ModelLayerRenderer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends LayerRenderer<T, M> {

    private static ArrayList<Class<? extends LivingEntity>> entitiesWithLayer = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderEntityPre(RenderLivingEvent.Pre e) {
        if (!entitiesWithLayer.contains(e.getEntity().getClass())) {
            e.getRenderer().addLayer(new ModelLayerRenderer(e.getRenderer()));
            entitiesWithLayer.add(e.getEntity().getClass());
        }
    }

    public ModelLayerRenderer(IEntityRenderer<T, M> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        renderItemLayers(entityIn, EquipmentSlotType.HEAD, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        renderItemLayers(entityIn, EquipmentSlotType.CHEST, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        renderItemLayers(entityIn, EquipmentSlotType.LEGS, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        renderItemLayers(entityIn, EquipmentSlotType.FEET, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

        ModelLayerContext context = new ModelLayerContext(entityIn);
        for (Ability ability : AbilityHelper.getAbilities(entityIn)) {
            if (ability instanceof IModelLayerProvider && ability.getConditionManager().isEnabled()) {
                renderLayers((IModelLayerProvider) ability, context, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
    }

    public void renderItemLayers(T entity, EquipmentSlotType slot, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        ModelLayerContext context = new ModelLayerContext(entity, stack, slot);

        if (stack.getItem() instanceof IModelLayerProvider) {
            for (IModelLayer layer : ((IModelLayerProvider) stack.getItem()).getModelLayers(context)) {
                if (layer.isActive(context)) {
                    GlStateManager.pushMatrix();
                    GlStateManager.color4f(1F, 1F, 1F, 1F);
                    layer.render(context, this.entityRenderer, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public void renderLayers(IModelLayerProvider provider, IModelLayerContext context, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (IModelLayer layer : provider.getModelLayers(context)) {
            if (layer.isActive(context)) {
                GlStateManager.pushMatrix();
                GlStateManager.color4f(1F, 1F, 1F, 1F);
                layer.render(context, this.entityRenderer, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}