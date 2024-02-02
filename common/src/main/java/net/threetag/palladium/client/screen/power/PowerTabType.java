package net.threetag.palladium.client.screen.power;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.icon.IIcon;

@Environment(EnvType.CLIENT)
public enum PowerTabType {

    ABOVE(0, 0, 28, 32, 8),
    BELOW(84, 0, 28, 32, 8),
    LEFT(0, 64, 32, 28, 5),
    RIGHT(96, 64, 32, 28, 5);

    public static final int MAX_TABS = java.util.Arrays.stream(values()).mapToInt(e -> e.max).sum();
    private final int textureX;
    private final int textureY;
    private final int width;
    private final int height;
    private final int max;

    PowerTabType(int j, int k, int l, int m, int n) {
        this.textureX = j;
        this.textureY = k;
        this.width = l;
        this.height = m;
        this.max = n;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(GuiGraphics guiGraphics, int offsetX, int offsetY, boolean isSelected, int index) {
        int i = this.textureX;
        if (index > 0) {
            i += this.width;
        }

        if (index == this.max - 1) {
            i += this.width;
        }

        int j = isSelected ? this.textureY + this.height : this.textureY;
        guiGraphics.blit(PowersScreen.TABS, offsetX + this.getX(index), offsetY + this.getY(index), i, j, this.width, this.height);
    }

    public void drawIcon(GuiGraphics guiGraphics, DataContext context, int offsetX, int offsetY, int index, IIcon icon) {
        int i = offsetX + this.getX(index);
        int j = offsetY + this.getY(index);
        switch (this) {
            case ABOVE -> {
                i += 6;
                j += 9;
            }
            case BELOW -> {
                i += 6;
                j += 6;
            }
            case LEFT -> {
                i += 10;
                j += 5;
            }
            case RIGHT -> {
                i += 6;
                j += 5;
            }
        }

        icon.draw(Minecraft.getInstance(), guiGraphics, context, i, j);
    }

    public int getX(int index) {
        switch (this) {
            case ABOVE:
            case BELOW:
                return (this.width + 4) * index;
            case LEFT:
                return -this.width + 4;
            case RIGHT:
                return 248;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public int getY(int index) {
        switch (this) {
            case ABOVE:
                return -this.height + 4;
            case BELOW:
                return 192;
            case LEFT:
            case RIGHT:
                return this.height * index;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public boolean isMouseOver(int offsetX, int offsetY, int index, double mouseX, double mouseY) {
        int i = offsetX + this.getX(index);
        int j = offsetY + this.getY(index);
        return mouseX > (double) i && mouseX < (double) (i + this.width) && mouseY > (double) j && mouseY < (double) (j + this.height);
    }
}
