package net.threetag.palladium.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.data.DataContext;
import net.threetag.palladium.data.DataContextType;

public record HasTagCondition(String tag) implements Condition {

    public static final MapCodec<HasTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.STRING.fieldOf("tag").forGetter(HasTagCondition::tag)
            ).apply(instance, HasTagCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, HasTagCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, HasTagCondition::tag, HasTagCondition::new
    );

    @Override
    public boolean active(DataContext context) {
        var entity = context.get(DataContextType.ENTITY);

        if (entity == null) {
            return false;
        }

        return entity.getTags().contains(this.tag);
    }

    @Override
    public ConditionSerializer<HasTagCondition> getSerializer() {
        return ConditionSerializers.HAS_TAG.get();
    }

    public static class Serializer extends ConditionSerializer<HasTagCondition> {

        @Override
        public MapCodec<HasTagCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HasTagCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity has a specific tag. These tags are added to entities via /tag command.";
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.DATA;
        }
    }
}
