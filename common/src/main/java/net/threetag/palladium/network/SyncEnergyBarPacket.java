package net.threetag.palladium.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.power.energybar.EnergyBar;
import net.threetag.palladium.power.energybar.EnergyBarReference;
import net.threetag.palladiumcore.network.NetworkManager;
import org.jetbrains.annotations.NotNull;

public record SyncEnergyBarPacket(int entityId, EnergyBarReference reference, int value,
                                  int maxValue) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncEnergyBarPacket> TYPE = new CustomPacketPayload.Type<>(Palladium.id("sync_energy_bar"));
    public static final StreamCodec<FriendlyByteBuf, SyncEnergyBarPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEnergyBarPacket::entityId,
            EnergyBarReference.STREAM_CODEC, SyncEnergyBarPacket::reference,
            ByteBufCodecs.VAR_INT, SyncEnergyBarPacket::value,
            ByteBufCodecs.VAR_INT, SyncEnergyBarPacket::maxValue,
            SyncEnergyBarPacket::new
    );

    public static void handle(SyncEnergyBarPacket packet, NetworkManager.Context context) {
        Entity entity = context.getPlayer().level().getEntity(packet.entityId);

        if (entity instanceof LivingEntity living) {
            EnergyBar energyBar = packet.reference.getBar(living);

            if (energyBar != null) {
                energyBar.set(packet.value);
                energyBar.setMax(packet.maxValue);
            }
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}