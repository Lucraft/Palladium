package net.threetag.palladium.condition;

import com.google.gson.JsonObject;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.util.context.DataContext;

public class AbilityFirstTickCondition extends Condition {

    private final AbilityReference ability;

    public AbilityFirstTickCondition(AbilityReference ability) {
        this.ability = ability;
    }

    @Override
    public boolean active(DataContext context) {
        var entity = context.getLivingEntity();
        var holder = context.getPowerHolder();

        if (entity == null) {
            return false;
        }

        AbilityInstance dependency = this.ability.getEntry(entity, holder);

        if (dependency == null) {
            return false;
        } else {
            return dependency.getEnabledTicks() == 1;
        }
    }

    @Override
    public ConditionSerializer getSerializer() {
        return ConditionSerializers.ABILITY_FIRST_TICK.get();
    }

    public static class Serializer extends ConditionSerializer {

        public Serializer() {
            this.withProperty(AbilityEnabledCondition.Serializer.POWER, null);
            this.withProperty(AbilityEnabledCondition.Serializer.ABILITY, "ability_id");
        }

        @Override
        public Condition make(JsonObject json) {
            AbilityReference abilityReference = AbilityReference.fromString(this.getProperty(json, AbilityEnabledCondition.Serializer.ABILITY));

            if (this.getProperty(json, AbilityEnabledCondition.Serializer.POWER) != null) {
                abilityReference = new AbilityReference(this.getProperty(json, AbilityEnabledCondition.Serializer.POWER), this.getProperty(json, AbilityEnabledCondition.Serializer.ABILITY));
            }

            return new AbilityFirstTickCondition(abilityReference);
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the ability is on its first tick.";
        }
    }
}
