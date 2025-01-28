package net.threetag.palladium.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.screen.abilitybar.AbilityBar;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerHolder;
import net.threetag.palladium.power.PowerUtil;
import net.threetag.palladium.power.PowerValidator;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SyncEntityPowersPacket(int entityId, List<Holder<Power>> remove,
                                     List<NewPowerChange> add) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncEntityPowersPacket> TYPE = new CustomPacketPayload.Type<>(Palladium.id("sync_entity_powers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityPowersPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEntityPowersPacket::entityId,
            ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.POWER).apply(ByteBufCodecs.list()), SyncEntityPowersPacket::remove,
            NewPowerChange.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncEntityPowersPacket::add,
            SyncEntityPowersPacket::new
    );

    @Override
    public @NotNull Type<SyncEntityPowersPacket> type() {
        return TYPE;
    }

    public static void handle(SyncEntityPowersPacket packet, NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.CLIENT) {
            context.queue(() -> handleClient(packet, context));
        }
    }

    public static SyncEntityPowersPacket create(LivingEntity entity, List<PowerHolder> removed, List<PowerHolder> added) {
        List<NewPowerChange> add = new ArrayList<>();
        added.forEach((powerHolder) -> add.add(new NewPowerChange(powerHolder)));
        return new SyncEntityPowersPacket(entity.getId(), removed.stream().map(PowerHolder::getPower).toList(), add);
    }

    public static SyncEntityPowersPacket create(LivingEntity entity) {
        List<NewPowerChange> add = new ArrayList<>();
        PowerUtil.getPowerHandler(entity).getPowerHolders().forEach((resourceLocation, powerHolder) -> {
            add.add(new NewPowerChange(powerHolder));
        });
        return new SyncEntityPowersPacket(entity.getId(), Collections.emptyList(), add);
    }

    @Environment(EnvType.CLIENT)
    public static void handleClient(SyncEntityPowersPacket packet, NetworkManager.PacketContext context) {
        Level level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(packet.entityId) instanceof LivingEntity livingEntity) {
            var handler = PowerUtil.getPowerHandler(livingEntity);

            for (Holder<Power> power : packet.remove) {
                handler.removePowerHolder(power);
            }

            for (NewPowerChange add : packet.add) {
                var powerHolder = new PowerHolder(livingEntity, add.power, PowerValidator.ALWAYS_ACTIVE, new CompoundTag());
                handler.addPowerHolder(powerHolder);

                for (Pair<String, DataComponentPatch> abilityComponent : add.abilityComponents) {
                    var ability = powerHolder.getAbilities().get(abilityComponent.getLeft());
                    if (ability != null) {
                        ability.applyPatch(abilityComponent.getRight());
                    }
                }

                for (Triple<String, Integer, Integer> pair : add.energyBars) {
                    var bar = powerHolder.getEnergyBars().get(pair.getLeft());

                    if (bar != null) {
                        bar.set(pair.getMiddle());
                        bar.setMax(pair.getRight());
                    }
                }
            }

            if (livingEntity == Minecraft.getInstance().player) {
                AbilityBar.INSTANCE.populate();
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class NewPowerChange {

        private static final StreamCodec<RegistryFriendlyByteBuf, Pair<String, DataComponentPatch>> PAIR_STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Pair::getLeft,
                DataComponentPatch.STREAM_CODEC, Pair::getRight,
                Pair::of
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, Triple<String, Integer, Integer>> TRIPLE_STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Triple::getLeft,
                ByteBufCodecs.VAR_INT, Triple::getMiddle,
                ByteBufCodecs.VAR_INT, Triple::getRight,
                Triple::of
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, NewPowerChange> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(PalladiumRegistryKeys.POWER), NewPowerChange::getPower,
                PAIR_STREAM_CODEC.apply(ByteBufCodecs.list()), NewPowerChange::getAbilityComponents,
                TRIPLE_STREAM_CODEC.apply(ByteBufCodecs.list()), NewPowerChange::getEnergyBars,
                NewPowerChange::new
        );

        public final Holder<Power> power;
        public final List<Pair<String, DataComponentPatch>> abilityComponents;
        public final List<Triple<String, Integer, Integer>> energyBars;

        public NewPowerChange(Holder<Power> power, List<Pair<String, DataComponentPatch>> abilityComponents, List<Triple<String, Integer, Integer>> energyBars) {
            this.power = power;
            this.abilityComponents = abilityComponents;
            this.energyBars = energyBars;
        }

        public NewPowerChange(PowerHolder powerHolder) {
            this.power = powerHolder.getPower();

            this.abilityComponents = new ArrayList<>();
            powerHolder.getAbilities().forEach((s, ability) -> {
                for (TypedDataComponent<?> component : ability.getComponents()) {
                    DataComponentType type = component.type();
                    DataComponentPatch patch = DataComponentPatch.builder().set(type, component.value()).build();
                    this.abilityComponents.add(Pair.of(ability.getReference().abilityKey(), patch));
                }
            });

            this.energyBars = new ArrayList<>();
            powerHolder.getEnergyBars().forEach((s, energyBar) -> {
                this.energyBars.add(Triple.of(energyBar.getReference().energyBarKey(), energyBar.get(), energyBar.getMax()));
            });
        }

        public Holder<Power> getPower() {
            return power;
        }

        public List<Pair<String, DataComponentPatch>> getAbilityComponents() {
            return abilityComponents;
        }

        public List<Triple<String, Integer, Integer>> getEnergyBars() {
            return energyBars;
        }
    }

}
