package net.threetag.palladium.util.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Registry;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class BlockTagProperty extends PalladiumProperty<TagKey<Block>> {

    public BlockTagProperty(String key) {
        super(key);
    }

    @Override
    public TagKey<Block> fromJSON(JsonElement jsonElement) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(jsonElement.getAsString()));
    }

    @Override
    public JsonElement toJSON(TagKey<Block> value) {
        return new JsonPrimitive(value.location().toString());
    }

    @Override
    public TagKey<Block> fromNBT(Tag tag, TagKey<Block> defaultValue) {
        if (tag instanceof StringTag stringTag) {
            return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(stringTag.getAsString()));
        }
        return defaultValue;
    }

    @Override
    public Tag toNBT(TagKey<Block> value) {
        return StringTag.valueOf(value.location().toString());
    }

    @Override
    public TagKey<Block> fromBuffer(FriendlyByteBuf buf) {
        return TagKey.create(Registry.BLOCK_REGISTRY, buf.readResourceLocation());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void toBuffer(FriendlyByteBuf buf, Object value) {
        buf.writeResourceLocation(((TagKey<Block>) value).location());
    }

    @Override
    public String getString(TagKey<Block> value) {
        return value == null ? null : value.location().toString();
    }

    @Override
    public String getPropertyType() {
        return "block_tag";
    }
}
