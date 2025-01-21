package net.threetag.palladium.power.ability.unlocking;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.data.DataContext;
import net.threetag.palladium.power.PowerHolder;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.Utils;

import java.util.Collections;
import java.util.List;

public class ConditionalUnlockingHandler extends UnlockingHandler {

    public static final ConditionalUnlockingHandler EMPTY = new ConditionalUnlockingHandler(Collections.emptyList());

    public final List<Condition> conditions;

    public ConditionalUnlockingHandler(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean check(LivingEntity entity, PowerHolder powerHolder, AbilityInstance<?> abilityInstance) {
        return Condition.checkConditions(this.conditions, DataContext.forAbility(entity, abilityInstance));
    }

    @Override
    public UnlockingHandlerSerializer<?> getSerializer() {
        return UnlockingHandlerSerializers.CONDITIONAL.get();
    }

    public static class Serializer extends UnlockingHandlerSerializer<ConditionalUnlockingHandler> {

        public static final MapCodec<ConditionalUnlockingHandler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Condition.LIST_CODEC.fieldOf("conditions").forGetter(h -> h.conditions)
        ).apply(instance, ConditionalUnlockingHandler::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalUnlockingHandler> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(Utils::newList, Condition.STREAM_CODEC), h -> h.conditions,
                ConditionalUnlockingHandler::new
        );

        @Override
        public MapCodec<ConditionalUnlockingHandler> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ConditionalUnlockingHandler> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
