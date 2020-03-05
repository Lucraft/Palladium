package net.threetag.threecore.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.threetag.threecore.potion.TCEffects;
import net.threetag.threecore.util.MathUtil;

import static net.minecraft.client.gui.AbstractGui.fill;

public class UnconsciousRenderer {

    private static int progress;
    private static int prevProgress;

    @SubscribeEvent
    public void renderHUD(RenderGameOverlayEvent.Post e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.HELMET && Minecraft.getInstance().gameSettings.thirdPersonView == 0) {
            float opacity = MathUtil.interpolate(prevProgress, progress, e.getPartialTicks()) / 50F;

            if (opacity > 0F) {
                GlStateManager.disableDepthTest();
                GlStateManager.disableAlphaTest();
                int width = Minecraft.getInstance().mainWindow.getScaledWidth();
                int height = Minecraft.getInstance().mainWindow.getScaledHeight();
                int color = (int) (255F * opacity) << 24;
                fill(0, 0, width, height, color);
                GlStateManager.enableAlphaTest();
                GlStateManager.enableDepthTest();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END && Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
            prevProgress = progress;
            if (Minecraft.getInstance().player.isPotionActive(TCEffects.UNCONSCIOUS.get()) && progress < 50) {
                progress++;
            } else if (!Minecraft.getInstance().player.isPotionActive(TCEffects.UNCONSCIOUS.get()) && progress > 0) {
                progress--;
            }
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent e) {
        if (progress > 0) {
            e.getMovementInput().rightKeyDown = false;
            e.getMovementInput().leftKeyDown = false;
            e.getMovementInput().forwardKeyDown = false;
            e.getMovementInput().backKeyDown = false;
            e.getMovementInput().sneak = false;
            e.getMovementInput().jump = false;
        }
    }

}