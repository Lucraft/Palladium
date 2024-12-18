package net.threetag.palladium.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.data.DataContext;

public class DayCondition implements Condition {

    public static final DayCondition INSTANCE = new DayCondition();

    public static final MapCodec<DayCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DayCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean active(DataContext context) {
        var level = context.getLevel();
        return level != null && level.isDay();
    }

    @Override
    public ConditionSerializer<DayCondition> getSerializer() {
        return ConditionSerializers.DAY.get();
    }

    public static class Serializer extends ConditionSerializer<DayCondition> {

        @Override
        public MapCodec<DayCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DayCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if it's currently daytime";
        }
    }
}
