package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.accessory.Accessory;
import net.threetag.palladium.accessory.AccessorySlot;
import net.threetag.palladium.accessory.RenderLayerAccessory;
import net.threetag.palladium.client.renderer.renderlayer.IPackRenderLayer;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RenderLayerByAccessorySlotAbility extends Ability implements RenderLayerProviderAbility {

    public static final MapCodec<RenderLayerByAccessorySlotAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("fallback_layer", null).forGetter(ab -> ab.fallbackLayer),
                    AccessorySlot.BY_NAME_CODEC.fieldOf("accessory_slot").forGetter(ab -> ab.slot),
                    propertiesCodec(), conditionsCodec(), energyBarUsagesCodec()
            ).apply(instance, RenderLayerByAccessorySlotAbility::new));

    public final ResourceLocation fallbackLayer;
    public final AccessorySlot slot;

    public RenderLayerByAccessorySlotAbility(ResourceLocation fallbackLayer, AccessorySlot slot, AbilityProperties properties, AbilityConditions conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.fallbackLayer = fallbackLayer;
        this.slot = slot;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public IPackRenderLayer getRenderLayer(AbilityInstance instance, LivingEntity entity, PackRenderLayerManager manager) {
        AtomicReference<ResourceLocation> layerId = new AtomicReference<>(this.fallbackLayer);

        if (entity instanceof Player player) {
            Accessory.getPlayerData(player).ifPresent(data -> {
                var slots = data.getSlots();

                if (this.slot != null && slots.containsKey(this.slot)) {
                    slots.get(this.slot).stream().filter(a -> a instanceof RenderLayerAccessory).findFirst().ifPresent(a -> {
                        layerId.set(((RenderLayerAccessory) a).renderLayerId);
                    });
                }
            });
        }

        return manager.getLayer(layerId.get());
    }

    @Override
    public AbilitySerializer<RenderLayerByAccessorySlotAbility> getSerializer() {
        return AbilitySerializers.RENDER_LAYER_BY_ACCESSORY_SLOT.get();
    }

    public static class Serializer extends AbilitySerializer<RenderLayerByAccessorySlotAbility> {

        @Override
        public MapCodec<RenderLayerByAccessorySlotAbility> codec() {
            return CODEC;
        }
    }
}
