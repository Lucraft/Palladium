package net.threetag.palladium.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.entity.PalladiumPlayerExtension;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;

public class IsHoveringOrFlyingCondition implements Condition {

    public static final IsHoveringOrFlyingCondition INSTANCE = new IsHoveringOrFlyingCondition();

    public static final MapCodec<IsHoveringOrFlyingCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsHoveringOrFlyingCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean active(DataContext context) {
        var entity = context.get(DataContextType.ENTITY);

        if (entity == null) {
            return false;
        }

        if (entity instanceof PalladiumPlayerExtension extension) {
            return extension.palladium$getFlightHandler().getHoveringAnimation(1F) > 0F || extension.palladium$getFlightHandler().getFlightAnimation(1F) > 0F;
        }
        return false;
    }

    @Override
    public ConditionSerializer<IsHoveringOrFlyingCondition> getSerializer() {
        return ConditionSerializers.IS_HOVERING_OR_FLYING.get();
    }

    public static class Serializer extends ConditionSerializer<IsHoveringOrFlyingCondition> {

        @Override
        public MapCodec<IsHoveringOrFlyingCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, IsHoveringOrFlyingCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity is hovering mid-air or flying.";
        }
    }
}
