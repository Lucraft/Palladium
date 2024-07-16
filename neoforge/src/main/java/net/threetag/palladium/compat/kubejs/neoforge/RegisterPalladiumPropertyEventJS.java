package net.threetag.palladium.compat.kubejs.neoforge;

import dev.latvian.mods.kubejs.level.KubeLevelEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import net.threetag.palladium.util.property.PalladiumProperty;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RegisterPalladiumPropertyEventJS implements KubeLevelEvent {

    private final Entity entity;
    private final EntityPropertyHandler handler;

    public RegisterPalladiumPropertyEventJS(Entity entity, EntityPropertyHandler handler) {
        this.entity = entity;
        this.handler = handler;
    }

    public ResourceLocation getEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(this.entity.getType());
    }

    public void registerProperty(String key, String type, Object defaultValue) {
        PalladiumProperty property = PalladiumPropertyLookup.get(type, key);

        if (property != null) {
            this.handler.register(property, PalladiumProperty.fixValues(property, defaultValue));
        }
    }

    @Override
    public Level getLevel() {
        return this.entity.level();
    }
}
