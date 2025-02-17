package net.threetag.palladium.condition;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.AbilityConfiguration;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;

import java.util.Objects;

public class ActionCondition extends KeyCondition {

    public ActionCondition(int cooldown, AbilityConfiguration.KeyType type, boolean needsEmptyHand, boolean allowScrollingWhenCrouching) {
        super(cooldown, type, needsEmptyHand, allowScrollingWhenCrouching);
    }

    @Override
    public boolean active(DataContext context) {
        var entity = context.getLivingEntity();
        var entry = context.get(DataContextType.ABILITY);

        if (entity == null || entry == null) {
            return false;
        }

        if (Objects.requireNonNull(entry).keyPressed) {
            entry.keyPressed = false;
            if (entry.getEnabledTicks() == 0 && this.cooldown != 0) {
                entry.startCooldown(entity, this.cooldown);
            }
            return true;
        }

        return false;
    }

    @Override
    public void onKeyPressed(LivingEntity entity, AbilityInstance entry, Power power, IPowerHolder holder) {
        if (entry.cooldown == 0) {
            entry.keyPressed = true;
        }
    }

    @Override
    public ConditionSerializer getSerializer() {
        return ConditionSerializers.ACTION.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Integer> COOLDOWN = new IntegerProperty("cooldown").configurable("Amount of ticks the ability wont be useable for after using it");

        public Serializer() {
            this.withProperty(COOLDOWN, 0);
            this.withProperty(KeyCondition.KEY_TYPE_WITH_SCROLLING, AbilityConfiguration.KeyType.KEY_BIND);
            this.withProperty(KeyCondition.NEEDS_EMPTY_HAND, false);
            this.withProperty(KeyCondition.ALLOW_SCROLLING_DURING_CROUCHING, true);
        }

        @Override
        public Condition make(JsonObject json) {
            return new ActionCondition(this.getProperty(json, COOLDOWN), this.getProperty(json, KeyCondition.KEY_TYPE_WITH_SCROLLING), this.getProperty(json, KeyCondition.NEEDS_EMPTY_HAND), this.getProperty(json, KeyCondition.ALLOW_SCROLLING_DURING_CROUCHING));
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.DATA;
        }

        @Override
        public String getDocumentationDescription() {
            return "This condition is used to activate the power when a key is pressed or a mouse button is clicked.";
        }
    }
}
