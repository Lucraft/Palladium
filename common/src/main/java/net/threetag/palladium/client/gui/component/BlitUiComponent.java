package net.threetag.palladium.client.gui.component;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public record BlitUiComponent(ResourceLocation texture, int u, int v, int uOffset, int vOffset, int texWidth,
                              int texHeight) implements UiComponent {

    @Override
    public int getWidth() {
        return this.uOffset;
    }

    @Override
    public int getHeight() {
        return this.vOffset;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics gui, DeltaTracker deltaTracker, int x, int y, UiAlignment alignment) {
        gui.blit(RenderType::guiTextured, this.texture, x, y, this.u, this.v, this.uOffset, this.vOffset, this.texWidth, this.texHeight);
    }
}
