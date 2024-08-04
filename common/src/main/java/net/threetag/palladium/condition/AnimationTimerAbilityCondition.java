package net.threetag.palladium.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.AnimationTimer;
import net.threetag.palladium.util.context.DataContext;

public record AnimationTimerAbilityCondition(AbilityReference ability, int min, int max) implements Condition {

    public static final MapCodec<AnimationTimerAbilityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    AbilityReference.CODEC.fieldOf("ability").forGetter(AnimationTimerAbilityCondition::ability),
                    Codec.INT.optionalFieldOf("min", Integer.MIN_VALUE).forGetter(AnimationTimerAbilityCondition::min),
                    Codec.INT.optionalFieldOf("max", Integer.MAX_VALUE).forGetter(AnimationTimerAbilityCondition::max)
            ).apply(instance, AnimationTimerAbilityCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationTimerAbilityCondition> STREAM_CODEC = StreamCodec.composite(
            AbilityReference.STREAM_CODEC, AnimationTimerAbilityCondition::ability,
            ByteBufCodecs.VAR_INT, AnimationTimerAbilityCondition::min,
            ByteBufCodecs.VAR_INT, AnimationTimerAbilityCondition::max,
            AnimationTimerAbilityCondition::new
    );

    @Override
    public boolean active(DataContext context) {
        var entity = context.getLivingEntity();
        var holder = context.getPowerHolder();

        if (entity == null) {
            return false;
        }

        AbilityInstance dependency = this.ability.getInstance(entity, holder);

        if (dependency == null || !(dependency.getConfiguration().getAbility() instanceof AnimationTimer animationTimer)) {
            return false;
        }

        var timer = (int) animationTimer.getAnimationTimer(dependency, 1F);
        return timer >= this.min && timer <= this.max;
    }

    @Override
    public ConditionSerializer<AnimationTimerAbilityCondition> getSerializer() {
        return ConditionSerializers.ANIMATION_TIMER_ABILITY.get();
    }

    public static class Serializer extends ConditionSerializer<AnimationTimerAbilityCondition> {

        @Override
        public MapCodec<AnimationTimerAbilityCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AnimationTimerAbilityCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the given animation timer ability has a certain value. This condition is a simplified version of the ability_integer_property condition designed to be used for animation timer abilities.";
        }
    }
}
