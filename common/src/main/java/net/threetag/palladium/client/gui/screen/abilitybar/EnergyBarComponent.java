package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.threetag.palladium.client.gui.component.UiAlignment;
import net.threetag.palladium.client.gui.component.UiComponent;
import net.threetag.palladium.power.energybar.EnergyBarInstance;

public class EnergyBarComponent implements UiComponent {

    private final EnergyBarInstance energyBarInstance;

    public EnergyBarComponent(EnergyBarInstance energyBarInstance) {
        this.energyBarInstance = energyBarInstance;
    }

    @Override
    public int getWidth() {
        return 10;
    }

    @Override
    public int getHeight() {
        return 112;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics gui, DeltaTracker deltaTracker, int x, int y, UiAlignment alignment) {
        int fullHeight = this.getHeight() - 6;
        int height = (int) ((energyBarInstance.get() / (float) energyBarInstance.getMax()) * fullHeight);

        gui.blit(RenderType::guiTextured, AbilityBar.TEXTURE, x, y, 152, 0, this.getWidth(), this.getHeight(), 256, 256);
        gui.blit(RenderType::guiTextured, AbilityBar.TEXTURE, x + 3, y + 3 + fullHeight - height, 162, fullHeight - height, this.getWidth() - 6, height, 256, 256, this.energyBarInstance.getConfiguration().color().getRGB());
    }
}
