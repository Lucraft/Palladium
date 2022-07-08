package net.threetag.palladium.registry.fabric;

import com.google.common.base.MoreObjects;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.registry.DeferredRegistry;
import net.threetag.palladium.registry.RegistrySupplier;

import java.util.function.Supplier;

public class DeferredRegistryImpl {

    public static <T> DeferredRegistry<T> create(String modid, ResourceKey<? extends Registry<T>> resourceKey) {
        return new Impl(modid, resourceKey);
    }

    public static class Impl<T> extends DeferredRegistry<T> {

        private final String modid;
        private final Registry<T> registry;

        @SuppressWarnings({"unchecked", "ConstantConditions"})
        public Impl(String modid, ResourceKey<? extends Registry<T>> resourceKey) {
            this.modid = modid;
            this.registry = (Registry<T>) MoreObjects.firstNonNull(Registry.REGISTRY.get(resourceKey.location()), BuiltinRegistries.REGISTRY.get(resourceKey.location()));
        }

        @Override
        public void register() {

        }

        @Override
        public <R extends T> RegistrySupplier<R> register(String id, Supplier<R> supplier) {
            ResourceLocation registeredId = new ResourceLocation(this.modid, id);
            return new RegistrySupplier<>(registeredId, Registry.register(this.registry, registeredId, supplier.get()));
        }
    }

}
