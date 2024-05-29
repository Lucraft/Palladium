package net.threetag.palladium.compat.geckolib.ability;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.renderer.renderlayer.CompoundPackRenderLayer;
import net.threetag.palladium.client.renderer.renderlayer.IPackRenderLayer;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;
import net.threetag.palladium.compat.geckolib.renderlayer.GeckoLayerState;
import net.threetag.palladium.entity.PalladiumLivingEntityExtension;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.ResourceLocationProperty;
import net.threetag.palladium.util.property.StringProperty;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.Collections;
import java.util.List;

public class RenderLayerAnimationAbility extends Ability {

    public static final PalladiumProperty<ResourceLocation> RENDER_LAYER = new ResourceLocationProperty("render_layer").configurable("Determines the ID of the render layer receiving the animation. Must be a gecko render layer!");
    public static final PalladiumProperty<String> CONTROLLER = new StringProperty("controller").configurable("Name of the animation controller the animation is played on. Leave it as 'main' if you didnt specify one.");
    public static final PalladiumProperty<String> ANIMATION_TRIGGER = new StringProperty("animation_trigger").configurable("Name of the animation trigger");

    public RenderLayerAnimationAbility() {
        this.withProperty(RENDER_LAYER, new ResourceLocation("test", "example_layer"));
        this.withProperty(CONTROLLER, "main");
        this.withProperty(ANIMATION_TRIGGER, "animation_trigger_name");
    }

    @Override
    public boolean isEffect() {
        return true;
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (enabled && entity.level().isClientSide && entity instanceof PalladiumLivingEntityExtension extension) {
            this.playAnimation(extension, entry);
        }
    }

    @Environment(EnvType.CLIENT)
    public void playAnimation(PalladiumLivingEntityExtension entity, AbilityInstance entry) {
        IPackRenderLayer layer = PackRenderLayerManager.getInstance().getLayer(entry.getProperty(RENDER_LAYER));
        if (layer != null) {
            List<IPackRenderLayer> layers;

            if (layer instanceof CompoundPackRenderLayer com) {
                layers = com.layers();
            } else {
                layers = Collections.singletonList(layer);
            }

            for (IPackRenderLayer renderLayer : layers) {
                var state = entity.palladium$getRenderLayerStates().get(renderLayer);
                if (state instanceof GeckoLayerState gecko) {
                    AnimatableManager<?> manager = gecko.getAnimatableInstanceCache().getManagerForId(gecko.hashCode() + ((Entity)entity).getId());
                    var controller = manager.getAnimationControllers().get(entry.getProperty(CONTROLLER));

                    if (controller != null) {
                        controller.forceAnimationReset();
                        controller.stop();
                        controller.tryTriggerAnimation(entry.getProperty(ANIMATION_TRIGGER));
                    }
                }
            }
        }
    }
}
