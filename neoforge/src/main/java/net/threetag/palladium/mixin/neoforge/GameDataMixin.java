package net.threetag.palladium.mixin.neoforge;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.GameData;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
@Mixin(GameData.class)
public class GameDataMixin {

    @Inject(method = "getRegistrationOrder",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0))
    private static void getRegistrationOrder(CallbackInfoReturnable<Set<ResourceLocation>> cir, @Local Set<ResourceLocation> ordered) {
        ordered.add(Registries.BLOCK_TYPE.location());
        ordered.add(PalladiumRegistryKeys.ITEM_TYPE.location());
    }

}
