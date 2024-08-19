package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.client.dynamictexture.TextureReference;
import net.threetag.palladium.client.renderer.entity.PlayerSkinHandler;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PlayerModelChangeType;
import net.threetag.palladium.util.SkinTypedValue;
import net.threetag.palladium.util.context.DataContext;

import java.util.List;

public class SkinChangeAbility extends Ability {

    public static final MapCodec<SkinChangeAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SkinTypedValue.codec(TextureReference.CODEC).fieldOf("texture").forGetter(ab -> ab.texture),
                    PlayerModelChangeType.CODEC.optionalFieldOf("model_type", PlayerModelChangeType.KEEP).forGetter(ab -> ab.modelChangeType),
                    Codec.INT.optionalFieldOf("priority", 50).forGetter(ab -> ab.priority),
                    propertiesCodec(), conditionsCodec(), energyBarUsagesCodec()
            ).apply(instance, SkinChangeAbility::new));

    public final SkinTypedValue<TextureReference> texture;
    public final PlayerModelChangeType modelChangeType;
    public final int priority;

    public SkinChangeAbility(SkinTypedValue<TextureReference> texture, PlayerModelChangeType modelChangeType, int priority, AbilityProperties properties, AbilityConditions conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.texture = texture;
        this.modelChangeType = modelChangeType;
        this.priority = priority;
    }

    @Override
    public AbilitySerializer<SkinChangeAbility> getSerializer() {
        return AbilitySerializers.SKIN_CHANGE.get();
    }

    @Environment(EnvType.CLIENT)
    public static class SkinProvider implements PlayerSkinHandler.SkinProvider {

        @Override
        public ResourceLocation getSkin(AbstractClientPlayer player, ResourceLocation previousSkin, ResourceLocation defaultSkin) {
            var abilities = AbilityUtil.getEnabledInstances(player, AbilitySerializers.SKIN_CHANGE.get()).stream().filter(AbilityInstance::isEnabled).sorted((a1, a2) -> ((SkinChangeAbility) a2.getAbility()).priority - ((SkinChangeAbility) a1.getAbility()).priority).toList();

            if (!abilities.isEmpty()) {
                var ability = abilities.getFirst();
                return ((SkinChangeAbility) ability.getAbility()).texture.get(player).getTexture(DataContext.forAbility(player, ability));
            }

            return previousSkin;
        }

        @Override
        public PlayerModelChangeType getModelType(AbstractClientPlayer player) {
            var abilities = AbilityUtil.getEnabledInstances(player, AbilitySerializers.SKIN_CHANGE.get()).stream().filter(AbilityInstance::isEnabled).sorted((a1, a2) -> ((SkinChangeAbility) a2.getAbility()).priority - ((SkinChangeAbility) a1.getAbility()).priority).toList();

            if (!abilities.isEmpty()) {
                var ability = abilities.getFirst();
                return ((SkinChangeAbility) ability.getAbility()).modelChangeType;
            }

            return PlayerSkinHandler.SkinProvider.super.getModelType(player);
        }
    }

    public static class Serializer extends AbilitySerializer<SkinChangeAbility> {

        @Override
        public MapCodec<SkinChangeAbility> codec() {
            return CODEC;
        }
    }

}
