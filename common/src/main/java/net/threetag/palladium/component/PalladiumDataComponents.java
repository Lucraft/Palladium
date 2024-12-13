package net.threetag.palladium.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.Palladium;
import net.threetag.palladiumcore.registry.DeferredRegister;
import net.threetag.palladiumcore.registry.RegistryHolder;

public class PalladiumDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Palladium.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    static {
        Items.RENDER_LAYERS.get();
        Abilities.UNLOCKED.get();
    }

    public static class Items {

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<ItemRenderLayers>> RENDER_LAYERS = DATA_COMPONENTS.register("render_layers", () -> DataComponentType.<ItemRenderLayers>builder()
                .persistent(ItemRenderLayers.CODEC)
                .networkSynchronized(ItemRenderLayers.STREAM_CODEC)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<CustomData>> BOTTLE_ENTITY_DATA = DATA_COMPONENTS.register("bottle_entity_data", () -> DataComponentType.<CustomData>builder()
                .persistent(CustomData.CODEC)
                .networkSynchronized(CustomData.STREAM_CODEC)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Integer>> OPENING = DATA_COMPONENTS.register("opening", () -> DataComponentType.<Integer>builder()
                .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build());
        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Boolean>> OPENED = DATA_COMPONENTS.register("opened", () -> DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build());
    }

    public static class Abilities {

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Boolean>> UNLOCKED = DATA_COMPONENTS.register("unlocked", () -> DataComponentType.<Boolean>builder()
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Boolean>> ENABLED = DATA_COMPONENTS.register("enabled", () -> DataComponentType.<Boolean>builder()
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Boolean>> KEY_PRESSED = DATA_COMPONENTS.register("key_pressed", () -> DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Integer>> COOLDOWN = DATA_COMPONENTS.register("cooldown", () -> DataComponentType.<Integer>builder()
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_COOLDOWN = DATA_COMPONENTS.register("max_cooldown", () -> DataComponentType.<Integer>builder()
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Integer>> ACTIVATED_TIME = DATA_COMPONENTS.register("activated_time", () -> DataComponentType.<Integer>builder()
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_ACTIVATED_TIME = DATA_COMPONENTS.register("max_activated_time", () -> DataComponentType.<Integer>builder()
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Boolean>> BOUGHT = DATA_COMPONENTS.register("bought", () -> DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Vec3>> ENERGY_BEAM_TARGET = DATA_COMPONENTS.register("energy_beam_target", () -> DataComponentType.<Vec3>builder()
                .networkSynchronized(ByteBufCodecs.VECTOR3F.map(Vec3::new, Vec3::toVector3f))
                .build());

        public static final RegistryHolder<DataComponentType<?>, DataComponentType<Boolean>> NAME_CHANGE_ACTIVE = DATA_COMPONENTS.register("name_change_active", () -> DataComponentType.<Boolean>builder()
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build());

        private static DataComponentMap COMMON_COMPONENTS = null;

        public static DataComponentMap getCommonComponents() {
            if (COMMON_COMPONENTS == null) {
                COMMON_COMPONENTS = DataComponentMap.builder()
                        .set(UNLOCKED.get(), true)
                        .set(ENABLED.get(), true)
                        .set(KEY_PRESSED.get(), false)
                        .set(COOLDOWN.get(), 0)
                        .set(MAX_COOLDOWN.get(), 0)
                        .set(ACTIVATED_TIME.get(), 0)
                        .set(MAX_ACTIVATED_TIME.get(), 0)
                        .build();
            }

            return COMMON_COMPONENTS;
        }
    }
}