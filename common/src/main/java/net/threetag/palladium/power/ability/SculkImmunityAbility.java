package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class SculkImmunityAbility extends Ability {

    public static final MapCodec<SculkImmunityAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec(), conditionsCodec(), energyBarUsagesCodec()
            ).apply(instance, SculkImmunityAbility::new));

    public SculkImmunityAbility(AbilityProperties properties, AbilityConditions conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
    }

    @Override
    public AbilitySerializer<SculkImmunityAbility> getSerializer() {
        return AbilitySerializers.SCULK_IMMUNITY.get();
    }

    public static class Serializer extends AbilitySerializer<SculkImmunityAbility> {

        @Override
        public MapCodec<SculkImmunityAbility> codec() {
            return CODEC;
        }
    }
}