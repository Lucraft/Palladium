package net.threetag.palladium.core.registry.neoforge;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.threetag.palladium.core.registry.DeferredRegister;
import net.threetag.palladium.core.registry.RegistryHolder;
import net.threetag.palladium.neoforge.PalladiumNeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DeferredRegisterImpl {

    public static <T> DeferredRegister<T> createInternal(String modid, ResourceKey<? extends Registry<T>> resourceKey) {
        return new Impl<>(modid, resourceKey);
    }

    public static class Impl<T> extends DeferredRegister<T> {

        private final String modid;
        private final net.neoforged.neoforge.registries.DeferredRegister<T> register;
        private final List<RegistryHolder<T>> entries;

        public Impl(String modid, ResourceKey<? extends Registry<T>> resourceKey) {
            this.modid = modid;
            this.register = net.neoforged.neoforge.registries.DeferredRegister.create(resourceKey, modid);
            this.entries = new ArrayList<>();
        }

        @Override
        public void register() {
            this.register.register(PalladiumNeoForge.getModEventBus(this.modid).orElseThrow(() -> new IllegalStateException("Mod '" + this.modid + "' did not register event bus to Palladium!")));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends T> RegistryHolder<R> register(String id, Supplier<R> supplier) {
            var orig = this.register.register(id, supplier);
            var registrySupplier = new RegistryHolderImpl<>(orig);
            this.entries.add(registrySupplier);
            return (RegistryHolder<R>) registrySupplier;
        }

        @Override
        public Collection<RegistryHolder<T>> getEntries() {
            return ImmutableList.copyOf(this.entries);
        }
    }

    public static class RegistryHolderImpl<T> extends RegistryHolder<T> {

        private final DeferredHolder<T, ? extends T> forgeHolder;

        public RegistryHolderImpl(DeferredHolder<T, ? extends T> forgeHolder) {
            this.forgeHolder = forgeHolder;
        }

        @Override
        public ResourceLocation getId() {
            return this.forgeHolder.getId();
        }

        @Override
        public @NotNull T value() {
            return this.forgeHolder.value();
        }

        @Override
        public boolean isBound() {
            return this.forgeHolder.isBound();
        }

        @Override
        public boolean is(@NotNull ResourceLocation location) {
            return this.forgeHolder.is(location);
        }

        @Override
        public boolean is(@NotNull ResourceKey<T> resourceKey) {
            return this.forgeHolder.is(resourceKey);
        }

        @Override
        public boolean is(@NotNull Predicate<ResourceKey<T>> predicate) {
            return this.forgeHolder.is(predicate);
        }

        @Override
        public boolean is(@NotNull TagKey<T> tagKey) {
            return this.forgeHolder.is(tagKey);
        }

        @Override
        public boolean is(Holder<T> holder) {
            return this.forgeHolder.is(holder);
        }

        @Override
        public @NotNull Stream<TagKey<T>> tags() {
            return this.forgeHolder.tags();
        }

        @Override
        public @NotNull Either<ResourceKey<T>, T> unwrap() {
            return this.forgeHolder.unwrap();
        }

        @Override
        public @NotNull Optional<ResourceKey<T>> unwrapKey() {
            return this.forgeHolder.unwrapKey();
        }

        @Override
        public @NotNull Holder.Kind kind() {
            return this.forgeHolder.kind();
        }

        @Override
        public boolean canSerializeIn(@NotNull HolderOwner<T> owner) {
            return this.forgeHolder.canSerializeIn(owner);
        }
    }
}
