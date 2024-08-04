package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class AbilityDescription {

    public static final Codec<AbilityDescription> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ComponentSerialization.CODEC.fieldOf("locked").forGetter(AbilityDescription::getLockedDescription),
                            ComponentSerialization.CODEC.fieldOf("unlocked").forGetter(AbilityDescription::getUnlockedDescription)
                    ).apply(instance, AbilityDescription::new)),
            ComponentSerialization.CODEC.xmap(AbilityDescription::new, AbilityDescription::getUnlockedDescription)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityDescription> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, AbilityDescription::getLockedDescription,
            ComponentSerialization.STREAM_CODEC, AbilityDescription::getUnlockedDescription,
            AbilityDescription::new
    );

    private final Component lockedDescription;
    private final Component unlockedDescription;

    public AbilityDescription(Component lockedDescription, Component unlockedDescription) {
        this.lockedDescription = lockedDescription;
        this.unlockedDescription = unlockedDescription;
    }

    public AbilityDescription(Component description) {
        this.lockedDescription = this.unlockedDescription = description;
    }

    public Component getLockedDescription() {
        return this.lockedDescription;
    }

    public Component getUnlockedDescription() {
        return this.unlockedDescription;
    }

    public Component get(boolean unlocked) {
        return unlocked ? this.unlockedDescription : this.lockedDescription;
    }

}
