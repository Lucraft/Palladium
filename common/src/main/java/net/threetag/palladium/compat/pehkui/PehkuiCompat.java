package net.threetag.palladium.compat.pehkui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.SizeAbility;
import net.threetag.palladium.util.SizeUtil;
import net.threetag.palladiumcore.event.EntityEvents;
import virtuoel.pehkui.api.*;

import java.util.Collection;

public class PehkuiCompat extends SizeUtil {

    public static final ScaleType ABILITY_SCALE = ScaleRegistries.register(ScaleRegistries.SCALE_TYPES, Palladium.id("ability"), ScaleType.Builder.create().affectsDimensions().build());
    public static final ScaleModifier ABILITY_MODIFIER = ScaleRegistries.register(ScaleRegistries.SCALE_MODIFIERS, Palladium.id("ability"), new TypedScaleModifier(() -> ABILITY_SCALE));

    public static void init() {
        SizeUtil.setInstance(new PehkuiCompat());
        ScaleTypes.BASE.getDefaultBaseValueModifiers().add(ABILITY_MODIFIER);
        EntityEvents.TICK_PRE.register(entity -> {
            if (entity instanceof LivingEntity living) {
                float scale = getAbilityMultiplier(living);

                if (scale != ABILITY_SCALE.getScaleData(entity).getTargetScale()) {
                    ABILITY_SCALE.getScaleData(entity).setTargetScale(scale);
                }
            }
        });
    }

    public static float getAbilityMultiplier(LivingEntity entity) {
        float f = 1F;

        try {
            for (AbilityInstance<SizeAbility> enabledEntry : AbilityUtil.getEnabledInstances(entity, AbilitySerializers.SIZE.get())) {
                f *= enabledEntry.getAbility().size;
            }
        } catch (Exception ignored) {

        }

        return f;
    }

    @Override
    public boolean startScaleChange(Entity entity, ResourceLocation scaleTypeId, float targetScale, int tickDelay) {
        var scaleType = ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, scaleTypeId);

        if (scaleType != null) {
            var data = scaleType.getScaleData(entity);
            data.setScaleTickDelay(tickDelay);
            data.setTargetScale(targetScale);
            return true;
        }

        return false;
    }

    @Override
    public float getWidthScale(Entity entity) {
        return ScaleTypes.HITBOX_WIDTH.getScaleData(entity).getScale();
    }

    @Override
    public float getWidthScale(Entity entity, float delta) {
        return ScaleTypes.HITBOX_WIDTH.getScaleData(entity).getScale(delta);
    }

    @Override
    public float getHeightScale(Entity entity) {
        return ScaleTypes.HITBOX_HEIGHT.getScaleData(entity).getScale();
    }

    @Override
    public float getHeightScale(Entity entity, float delta) {
        return ScaleTypes.HITBOX_HEIGHT.getScaleData(entity).getScale(delta);
    }

    @Override
    public float getModelWidthScale(Entity entity) {
        return ScaleTypes.MODEL_WIDTH.getScaleData(entity).getScale();
    }

    @Override
    public float getModelWidthScale(Entity entity, float delta) {
        return ScaleTypes.MODEL_WIDTH.getScaleData(entity).getScale(delta);
    }

    @Override
    public float getModelHeightScale(Entity entity) {
        return ScaleTypes.MODEL_HEIGHT.getScaleData(entity).getScale();
    }

    @Override
    public float getModelHeightScale(Entity entity, float delta) {
        return ScaleTypes.MODEL_HEIGHT.getScaleData(entity).getScale(delta);
    }

    @Override
    public float getEyeHeightScale(Entity entity) {
        return ScaleTypes.EYE_HEIGHT.getScaleData(entity).getScale();
    }

    @Override
    public float getEyeHeightScale(Entity entity, float delta) {
        return ScaleTypes.EYE_HEIGHT.getScaleData(entity).getScale(delta);
    }

    @Override
    public Collection<ResourceLocation> getScaleTypeIds() {
        return ScaleRegistries.SCALE_TYPES.keySet();
    }
}
