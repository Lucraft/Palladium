package net.threetag.palladium.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.util.context.DataContext;

public class OnGroundCondition implements Condition {

    public static final OnGroundCondition INSTANCE = new OnGroundCondition();

    public static final MapCodec<OnGroundCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, OnGroundCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean active(DataContext context) {
        var entity = context.getEntity();
        return entity != null && entity.onGround();
    }

    @Override
    public ConditionSerializer<OnGroundCondition> getSerializer() {
        return ConditionSerializers.ON_GROUND.get();
    }

    public static class Serializer extends ConditionSerializer<OnGroundCondition> {

        @Override
        public MapCodec<OnGroundCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OnGroundCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity is on the ground.";
        }
    }
}
