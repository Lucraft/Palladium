package net.threetag.palladium.client.icon;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.registry.PalladiumRegistries;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.data.DataContext;

public interface Icon {

    Codec<Icon> CODEC = Codec.withAlternative(
            PalladiumRegistries.ICON_SERIALIZER.byNameCodec().dispatch(Icon::getSerializer, IconSerializer::codec),
            Codec.STRING.comapFlatMap(Icon::read, Icon::toSimpleString).stable()
    );

    StreamCodec<RegistryFriendlyByteBuf, Icon> STREAM_CODEC = ByteBufCodecs.registry(PalladiumRegistryKeys.ICON_SERIALIZER).dispatch(Icon::getSerializer, IconSerializer::streamCodec);

    static Icon parse(String input) {
        if (input.endsWith(".png")) {
            return new TexturedIcon(ResourceLocation.parse(input));
        } else if (input.startsWith("#")) {
            return new TexturedIcon(TextureReference.parse(input));
        } else {
            ResourceLocation id = ResourceLocation.parse(input);

            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                throw new JsonParseException("Unknown item '" + input + "'");
            }

            return new ItemIcon(BuiltInRegistries.ITEM.getValue(id));
        }
    }

    static DataResult<Icon> read(String path) {
        try {
            return DataResult.success(parse(path));
        } catch (ResourceLocationException e) {
            return DataResult.error(() -> "Not a valid texture reference: " + path + " " + e.getMessage());
        }
    }

    static String toSimpleString(Icon icon) {
        if (icon instanceof ItemIcon itemIcon) {
            return BuiltInRegistries.ITEM.getKey(itemIcon.stack.getItem()).toString();
        } else if (icon instanceof TexturedIcon texturedIcon && texturedIcon.tint == null) {
            return texturedIcon.texture.toString();
        } else {
            return "invalid";
        }
    }

    @Environment(EnvType.CLIENT)
    default void draw(Minecraft mc, GuiGraphics guiGraphics, DataContext context, int x, int y) {
        this.draw(mc, guiGraphics, context, x, y, 16, 16);
    }

    @Environment(EnvType.CLIENT)
    void draw(Minecraft mc, GuiGraphics guiGraphics, DataContext context, int x, int y, int width, int height);

    IconSerializer<?> getSerializer();

}
