package net.threetag.palladium.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.compat.curiostinkets.CuriosTrinketsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class PlayerSlot {

    public static final Codec<PlayerSlot> CODEC = Codec.STRING.xmap(PlayerSlot::get, Object::toString);
    public static final StreamCodec<FriendlyByteBuf, PlayerSlot> STREAM_CODEC = StreamCodec.of((buf, slot) -> buf.writeUtf(slot.toString()), buf -> Objects.requireNonNull(PlayerSlot.get(buf.readUtf())));

    private static final Map<EquipmentSlot, PlayerSlot> EQUIPMENT_SLOTS = new HashMap<>();
    private static final Map<String, PlayerSlot> SLOTS = new HashMap<>();

    @NotNull
    public static PlayerSlot get(EquipmentSlot slot) {
        return Objects.requireNonNull(get(slot.getName()));
    }

    @Nullable
    public static PlayerSlot get(String name) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getName().equalsIgnoreCase(name)) {
                return EQUIPMENT_SLOTS.computeIfAbsent(slot, EquipmentPlayerSlot::new);
            }
        }

        if (name.startsWith("curios:")) {
            return SLOTS.computeIfAbsent(name, n -> new CurioTrinketSlot(true, name.substring("curios:".length())));
        }

        if (name.startsWith("trinkets:")) {
            return SLOTS.computeIfAbsent(name, n -> new CurioTrinketSlot(false, name.substring("trinkets:".length())));
        }

        return null;
    }

    public abstract List<ItemStack> getItems(LivingEntity entity);

    public abstract void setItem(LivingEntity entity, ItemStack stack);

    public abstract void clear(LivingEntity entity);

    public abstract Type getType();

    @Nullable
    public EquipmentSlot getEquipmentSlot() {
        return null;
    }

    private static class EquipmentPlayerSlot extends PlayerSlot {

        private final EquipmentSlot slot;

        public EquipmentPlayerSlot(EquipmentSlot slot) {
            this.slot = slot;
        }

        @Override
        public List<ItemStack> getItems(LivingEntity entity) {
            return Collections.singletonList(entity.getItemBySlot(this.slot));
        }

        @Override
        public void setItem(LivingEntity entity, ItemStack stack) {
            entity.setItemSlot(this.slot, stack);
        }

        @Override
        public void clear(LivingEntity entity) {
            entity.setItemSlot(this.slot, ItemStack.EMPTY);
        }

        @Override
        public @Nullable EquipmentSlot getEquipmentSlot() {
            return this.slot;
        }

        @Override
        public Type getType() {
            return Type.EQUIPMENT_SLOT;
        }

        @Override
        public String toString() {
            return this.slot.getName();
        }
    }

    private static class CurioTrinketSlot extends PlayerSlot {

        private final boolean curios;
        private final String slot;

        public CurioTrinketSlot(boolean curios, String slot) {
            this.curios = curios;
            this.slot = slot;
        }

        @Override
        public List<ItemStack> getItems(LivingEntity entity) {
            return CuriosTrinketsUtil.getInstance().getItemsInSlot(entity, this.slot);
        }

        @Override
        public void setItem(LivingEntity entity, ItemStack stack) {
            CuriosTrinketsUtil.getInstance().getSlot(entity, this.slot).setStackInSlot(0, stack);
        }

        @Override
        public void clear(LivingEntity entity) {
            var inv = CuriosTrinketsUtil.getInstance().getSlot(entity, this.slot);
            for (int i = 0; i < inv.getSlots(); i++) {
                inv.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        @Override
        public Type getType() {
            return Type.CURIOS_TRINKET;
        }

        @Override
        public String toString() {
            return (this.curios ? "curios:" : "trinkets:") + this.slot;
        }
    }

    public enum Type {

        EQUIPMENT_SLOT,
        CURIOS_TRINKET

    }

}
