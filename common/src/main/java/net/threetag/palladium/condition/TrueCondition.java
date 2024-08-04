package net.threetag.palladium.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.util.context.DataContext;

public class TrueCondition implements Condition {

    public static final TrueCondition INSTANCE = new TrueCondition();

    public static final MapCodec<TrueCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, TrueCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean active(DataContext context) {
        return true;
    }

    @Override
    public ConditionSerializer<TrueCondition> getSerializer() {
        return ConditionSerializers.TRUE.get();
    }

    public static class Serializer extends ConditionSerializer<TrueCondition> {

        @Override
        public MapCodec<TrueCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TrueCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "It's just true. That's it.";
        }
    }

}
