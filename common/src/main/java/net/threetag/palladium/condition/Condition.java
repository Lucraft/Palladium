package net.threetag.palladium.condition;

import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.AbilityConfiguration;
import net.threetag.palladium.util.property.PropertyManager;

import java.util.Collections;
import java.util.List;

public abstract class Condition {

    private ConditionEnvironment environment;

    public Condition setEnvironment(ConditionEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public ConditionEnvironment getEnvironment() {
        return environment;
    }

    public abstract boolean active(DataContext context);

    public boolean needsKey() {
        return false;
    }

    public AbilityConfiguration.KeyType getKeyType() {
        return AbilityConfiguration.KeyType.KEY_BIND;
    }

    public AbilityConfiguration.KeyPressType getKeyPressType() {
        return AbilityConfiguration.KeyPressType.ACTION;
    }

    public boolean handlesCooldown() {
        return false;
    }

    public CooldownType getCooldownType() {
        return CooldownType.STATIC;
    }

    public void init(LivingEntity entity, AbilityInstance entry, PropertyManager manager) {

    }

    public void registerAbilityProperties(AbilityInstance entry, PropertyManager manager) {

    }

    public void onKeyPressed(LivingEntity entity, AbilityInstance entry, Power power, IPowerHolder holder) {

    }

    public void onKeyReleased(LivingEntity entity, AbilityInstance entry, Power power, IPowerHolder holder) {

    }

    public abstract ConditionSerializer getSerializer();

    public List<String> getDependentAbilities() {
        return Collections.emptyList();
    }
}
