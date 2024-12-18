package net.threetag.palladium.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.data.DataContext;
import net.threetag.palladium.data.DataContextType;

public record EntityTypeTagCondition(TagKey<EntityType<?>> tag) implements Condition {

    public static final MapCodec<EntityTypeTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(TagKey.codec(Registries.ENTITY_TYPE).fieldOf("entity_type_tag").forGetter(EntityTypeTagCondition::tag)
            ).apply(instance, EntityTypeTagCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityTypeTagCondition> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.map(loc -> TagKey.create(Registries.ENTITY_TYPE, loc), TagKey::location), EntityTypeTagCondition::tag, EntityTypeTagCondition::new
    );

    @Override
    public boolean active(DataContext context) {
        var entity = context.get(DataContextType.ENTITY);

        if (entity == null) {
            return false;
        }

        return entity.getType().is(this.tag);
    }

    @Override
    public ConditionSerializer<EntityTypeTagCondition> getSerializer() {
        return ConditionSerializers.ENTITY_TYPE_TAG.get();
    }

    public static class Serializer extends ConditionSerializer<EntityTypeTagCondition> {

        @Override
        public MapCodec<EntityTypeTagCondition> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntityTypeTagCondition> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity is of a certain tag.";
        }
    }
}
