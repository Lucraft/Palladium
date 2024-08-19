package net.threetag.palladium.mixin.client;

import net.minecraft.client.model.geom.ModelPart;
import net.threetag.palladium.client.renderer.entity.HumanoidRendererModifications;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin {

    @Shadow
    @Final
    public Map<String, ModelPart> children;

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private int injected(int color) {
        if (HumanoidRendererModifications.ALPHA_MULTIPLIER < 1F) {
            var c = new Color(color, true);
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * HumanoidRendererModifications.ALPHA_MULTIPLIER)).getRGB();
        } else {
            return color;
        }
    }

    @Inject(method = "getChild", at = @At("HEAD"), cancellable = true)
    public void getChild(String name, CallbackInfoReturnable<ModelPart> cir) {
        ModelPart modelPart = this.children.get(name);
        if (modelPart == null) {
            cir.setReturnValue(new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        }
    }

}
