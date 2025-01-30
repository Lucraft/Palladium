package net.threetag.palladium.power.ability.keybind;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.client.PalladiumKeyMappings;
import net.threetag.palladium.client.gui.component.TextUiComponent;
import net.threetag.palladium.client.gui.component.UiComponent;
import net.threetag.palladium.power.ability.AbilityInstance;

public class AbilityKeyBind extends KeyBindType {

    public static final AbilityKeyBind INSTANCE = new AbilityKeyBind();
    public static final MapCodec<AbilityKeyBind> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityKeyBind> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public KeyBindTypeSerializer<?> getSerializer() {
        return KeyBindTypeSerializers.ABILITY_KEY.get();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UiComponent getDisplayedKey(AbilityInstance<?> abilityInstance, ResourceLocation texture, boolean inside, int index) {
        return new TextUiComponent(PalladiumKeyMappings.ABILITY_KEYS[index].getTranslatedKeyMessage(), inside);
    }

    public static class Serializer extends KeyBindTypeSerializer<AbilityKeyBind> {

        @Override
        public MapCodec<AbilityKeyBind> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AbilityKeyBind> streamCodec() {
            return STREAM_CODEC;
        }
    }

}
