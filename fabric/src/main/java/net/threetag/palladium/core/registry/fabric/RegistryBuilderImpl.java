package net.threetag.palladium.core.registry.fabric;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.threetag.palladium.core.registry.RegistryBuilder;

public class RegistryBuilderImpl {

    public static <T> Registry<T> createRegistry(RegistryBuilder<T> registryBuilder) {
        var builder = registryBuilder.getDefaultKey() != null ? FabricRegistryBuilder.createDefaulted(registryBuilder.getResourceKey(), registryBuilder.getDefaultKey()) : FabricRegistryBuilder.createSimple(registryBuilder.getResourceKey());

        if (registryBuilder.isSynced()) {
            builder.attribute(RegistryAttribute.SYNCED);
        }

        return builder.buildAndRegister();
    }

}
