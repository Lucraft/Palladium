package net.threetag.palladium.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.threetag.palladium.client.PalladiumKeyMappings;
import net.threetag.palladium.network.AbilityKeyPressedPacket;
import net.threetag.palladium.power.ability.AbilityConditions;
import net.threetag.palladium.power.ability.AbilityConfiguration;
import net.threetag.palladiumcore.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    public LocalPlayer player;

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasMissTime()Z"), cancellable = true)
    private void startAttackStartAbility(CallbackInfoReturnable<Boolean> cir) {
        if (PalladiumKeyMappings.LEFT_CLICKED_ABILITY == null) {
            var entry = PalladiumKeyMappings.getPrioritisedKeyedAbility(AbilityConditions.KeyType.LEFT_CLICK);

            if (entry != null && entry.isUnlocked() && (!entry.getAbility().getConditions().needsEmptyHand() || this.player.getMainHandItem().isEmpty())) {
                var pressType = entry.getAbility().getConditions().getKeyPressType();

                if (pressType == AbilityConditions.KeyPressType.ACTION) {
                    if (!entry.isOnCooldown()) {
                        NetworkManager.get().sendToServer(new AbilityKeyPressedPacket(entry.getReference(), true));
                        PalladiumKeyMappings.LEFT_CLICKED_ABILITY = entry;
                        cir.setReturnValue(false);
                    }
                } else if (pressType == AbilityConditions.KeyPressType.ACTIVATION) {
                    if (!entry.isOnCooldown() && !entry.isEnabled()) {
                        NetworkManager.get().sendToServer(new AbilityKeyPressedPacket(entry.getReference(), true));
                        PalladiumKeyMappings.LEFT_CLICKED_ABILITY = entry;
                        cir.setReturnValue(false);
                    }
                } else {
                    NetworkManager.get().sendToServer(new AbilityKeyPressedPacket(entry.getReference(), true));
                    PalladiumKeyMappings.LEFT_CLICKED_ABILITY = entry;
                    cir.setReturnValue(false);
                }
            }
        } else {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void startAttackStopAbility(CallbackInfoReturnable<Boolean> cir) {
        if (PalladiumKeyMappings.LEFT_CLICKED_ABILITY != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startUseItem", at = @At("TAIL"), cancellable = true)
    private void startUseItemStartAbility(CallbackInfo ci) {
        if (PalladiumKeyMappings.RIGHT_CLICKED_ABILITY == null) {
            var entry = PalladiumKeyMappings.getPrioritisedKeyedAbility(AbilityConditions.KeyType.RIGHT_CLICK);

            if (entry != null && entry.isUnlocked() && (!entry.getAbility().getConditions().needsEmptyHand() || this.player.getMainHandItem().isEmpty())) {
                var pressType = entry.getAbility().getConditions().getKeyPressType();

                if (pressType == AbilityConditions.KeyPressType.ACTION) {
                    if (!entry.isOnCooldown()) {
                        NetworkManager.get().sendToServer(new AbilityKeyPressedPacket(entry.getReference(), true));
                        PalladiumKeyMappings.RIGHT_CLICKED_ABILITY = entry;
                        ci.cancel();
                    }
                } else if (pressType == AbilityConditions.KeyPressType.ACTIVATION) {
                    if (!entry.isOnCooldown() && !entry.isEnabled()) {
                        NetworkManager.get().sendToServer(new AbilityKeyPressedPacket(entry.getReference(), true));
                        PalladiumKeyMappings.RIGHT_CLICKED_ABILITY = entry;
                        ci.cancel();
                    }
                } else {
                    NetworkManager.get().sendToServer(new AbilityKeyPressedPacket(entry.getReference(), true));
                    PalladiumKeyMappings.RIGHT_CLICKED_ABILITY = entry;
                    ci.cancel();
                }
            }

        } else {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void startUseItemStopAbility(CallbackInfo ci) {
        if (PalladiumKeyMappings.RIGHT_CLICKED_ABILITY != null) {
            ci.cancel();
        }
    }

}
