package net.threetag.palladium.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.client.dynamictexture.EntityDynamicTexture;
import net.threetag.palladium.client.renderer.entity.PlayerSkinHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow
    @Final
    private GameProfile profile;

    @Inject(at = @At("RETURN"), method = "getSkin", cancellable = true)
    public void getSkinLocation(CallbackInfoReturnable<PlayerSkin> ci) {
        if (!EntityDynamicTexture.IGNORE_SKIN_CHANGE) {
            var original = ci.getReturnValue();
            var overridden = PlayerSkinHandler.getCurrentSkin(this.profile, original);

            if (!original.equals(overridden)) {
                ci.setReturnValue(overridden);
            }
        }
    }

}
