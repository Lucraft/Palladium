package net.threetag.palladium.client.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.data.DataContext;
import net.threetag.palladium.util.CodecExtras;

import java.awt.*;
import java.util.Objects;

public class TexturedIcon implements Icon {

    public static final MapCodec<TexturedIcon> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
            .group(
                    TextureReference.CODEC.fieldOf("texture").forGetter(TexturedIcon::getTexture),
                    CodecExtras.COLOR_CODEC.optionalFieldOf("texture", Color.WHITE).forGetter(TexturedIcon::getTint)
            )
            .apply(instance, TexturedIcon::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TexturedIcon> STREAM_CODEC = StreamCodec.composite(
            TextureReference.STREAM_CODEC, icon -> icon.texture,
            CodecExtras.COLOR_STREAM_CODEC, icon -> icon.tint,
            TexturedIcon::new
    );

    public static final ResourceLocation LOCK = Palladium.id("textures/icon/lock.png");

    public final TextureReference texture;
    public final Color tint;

    public TexturedIcon(TextureReference texture) {
        this(texture, Color.WHITE);
    }

    public TexturedIcon(TextureReference texture, Color tint) {
        this.texture = texture;
        this.tint = tint;
    }

    public TexturedIcon(ResourceLocation texture) {
        this(texture, Color.WHITE);
    }

    public TexturedIcon(ResourceLocation texture, Color tint) {
        this(TextureReference.normal(texture), tint);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void draw(Minecraft mc, GuiGraphics guiGraphics, DataContext context, int x, int y, int w, int h) {
        // TODO test
        var sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(Objects.requireNonNull(this.texture.getTexture(context)));
        var m = guiGraphics.pose().last().pose();

        var r = this.tint.getRed();
        var g = this.tint.getGreen();
        var b = this.tint.getBlue();
        var a = this.tint.getAlpha();

        var minU = sprite.getU0();
        var minV = sprite.getV0();
        var maxU = sprite.getU1();
        var maxV = sprite.getV1();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(m, x, y, 0F)
                .setUv(minU, minV)
                .setColor(r, g, b, a);
        buffer.addVertex(m, x, y + h, 0F)
                .setUv(minU, maxV)
                .setColor(r, g, b, a);
        buffer.addVertex(m, x + w, y + h, 0F)
                .setUv(maxU, maxV)
                .setColor(r, g, b, a);
        buffer.addVertex(m, x + w, y, 0F)
                .setUv(maxU, minV)
                .setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public TextureReference getTexture() {
        return texture;
    }

    public Color getTint() {
        return tint;
    }

    @Override
    public IconSerializer<TexturedIcon> getSerializer() {
        return IconSerializers.TEXTURE.get();
    }

    @Override
    public String toString() {
        return "TexturedIcon{" +
                "texture=" + texture +
                ", tint=" + tint +
                '}';
    }

    public static class Serializer extends IconSerializer<TexturedIcon> {

        @Override
        public MapCodec<TexturedIcon> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TexturedIcon> streamCodec() {
            return STREAM_CODEC;
        }
    }
}