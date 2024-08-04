package net.threetag.palladium.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.entity.EffectEntity;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.PalladiumPropertyBuilder;
import net.threetag.palladium.util.property.PalladiumPropertyType;
import net.threetag.palladium.util.property.PropertyManager;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class EntityEffect {

    public static final PalladiumProperty<Boolean> IS_DONE_PLAYING = PalladiumPropertyBuilder.create("is_done_playing", PalladiumPropertyType.BOOLEAN).build();

    public void registerProperties(PropertyManager manager) {
        manager.register(IS_DONE_PLAYING, false);
    }

    @Environment(EnvType.CLIENT)
    public abstract void render(EffectEntity entity, Entity anchor, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, boolean isFirstPerson, float partialTicks);

    public abstract void tick(EffectEntity entity, Entity anchor);

    public boolean isInRangeToRenderDist(EffectEntity effectEntity, Entity anchor, double distance) {
        return anchor.shouldRenderAtSqrDistance(distance);
    }

    public <T> T get(EffectEntity entity, PalladiumProperty<T> property) {
        return property.get(entity);
    }

    public <T> void set(EffectEntity entity, PalladiumProperty<T> property, T value) {
        property.set(entity, value);
    }

    public boolean isPlaying(EffectEntity entity) {
        return !this.get(entity, IS_DONE_PLAYING);
    }

    public void stopPlaying(EffectEntity entity) {
        this.set(entity, IS_DONE_PLAYING, true);
    }

    @Environment(EnvType.CLIENT)
    public static void start(Entity anchor, EntityEffect entityEffect) {
        EffectEntity effectEntity = new EffectEntity(anchor.level(), anchor, entityEffect);
        Objects.requireNonNull(Minecraft.getInstance().level).addEntity(effectEntity);
    }

    @Environment(EnvType.CLIENT)
    public static void stop(Entity anchor, Predicate<EntityEffect> predicate) {
        anchor.level().getEntities(anchor, anchor.getBoundingBox().inflate(2), entity -> entity instanceof EffectEntity && ((EffectEntity) entity).getAnchorEntity() == anchor && predicate.test(((EffectEntity) entity).entityEffect)).forEach(Entity::discard);
    }

    public static void stop(Entity anchor, EntityEffect entityEffectType) {
        stop(anchor, effect -> effect == entityEffectType);
    }

}
