package net.threetag.palladium.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;

public class SprintingCondition implements Condition {

    public static final SprintingCondition INSTANCE = new SprintingCondition();

    public static final MapCodec<SprintingCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SprintingCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean active(DataContext context) {
        var entity = context.get(DataContextType.ENTITY);

        if (entity == null) {
            return false;
        }

        return entity.isSprinting();
    }

    @Override
    public ConditionSerializer<SprintingCondition> getSerializer() {
        return ConditionSerializers.SPRINTING.get();
    }

    public static class Serializer extends ConditionSerializer<SprintingCondition> {

        @Override
        public MapCodec<SprintingCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SprintingCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity is sprinting.";
        }
    }
}
