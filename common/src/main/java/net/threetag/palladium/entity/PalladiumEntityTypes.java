package net.threetag.palladium.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.threetag.palladium.Palladium;
import net.threetag.palladiumcore.registry.DeferredRegister;
import net.threetag.palladiumcore.registry.EntityAttributeRegistry;
import net.threetag.palladiumcore.registry.RegistryHolder;

import java.util.function.Supplier;

public class PalladiumEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Palladium.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistryHolder<EntityType<?>, EntityType<EffectEntity>> EFFECT = register("effect", () -> EntityType.Builder.<EffectEntity>of(EffectEntity::new, MobCategory.MISC).sized(0.1F, 0.1F));
    public static final RegistryHolder<EntityType<?>, EntityType<TrailSegmentEntity<?>>> TRAIL_SEGMENT = register("trail_segment", () -> EntityType.Builder.<TrailSegmentEntity<?>>of(TrailSegmentEntity::new, MobCategory.MISC).sized(0.6F, 1.8F));
    public static final RegistryHolder<EntityType<?>, EntityType<CustomProjectile>> CUSTOM_PROJECTILE = register("custom_projectile", () -> EntityType.Builder.of(CustomProjectile::new, MobCategory.MISC).sized(0.1F, 0.1F).clientTrackingRange(4).updateInterval(10));
    public static final RegistryHolder<EntityType<?>, EntityType<SuitStand>> SUIT_STAND = register("suit_stand", () -> EntityType.Builder.<SuitStand>of(SuitStand::new, MobCategory.MISC).sized(0.6F, 1.8F));

    public static void init() {
        EntityAttributeRegistry.register(SUIT_STAND, SuitStand::createLivingAttributes);
        EntityAttributeRegistry.register(TRAIL_SEGMENT, SuitStand::createLivingAttributes);
    }

    public static <T extends Entity> RegistryHolder<EntityType<?>, EntityType<T>> register(String id, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITIES.register(id, () -> builderSupplier.get().build(Palladium.MOD_ID + ":" + id));
    }
}
