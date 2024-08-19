package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class TrailAbility extends Ability {

    public static final MapCodec<TrailAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("trail").forGetter(ab -> ab.trailRendererId),
                    propertiesCodec(), conditionsCodec(), energyBarUsagesCodec()
            ).apply(instance, TrailAbility::new));

    public final ResourceLocation trailRendererId;

    public TrailAbility(ResourceLocation trailRendererId, AbilityProperties properties, AbilityConditions conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.trailRendererId = trailRendererId;
    }

    @Override
    public AbilitySerializer<TrailAbility> getSerializer() {
        return AbilitySerializers.TRAIL.get();
    }

    public static class Serializer extends AbilitySerializer<TrailAbility> {

        @Override
        public MapCodec<TrailAbility> codec() {
            return CODEC;
        }
    }
}
