package net.threetag.palladium.condition;

import com.google.gson.JsonObject;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.property.*;

import java.util.Objects;

public class IntervalCondition extends Condition {

    public static final PalladiumProperty<Integer> TICKS = new IntegerProperty("interval_ticks").sync(SyncType.NONE);
    public static final PalladiumProperty<Boolean> ACTIVE = new BooleanProperty("interval_active");

    private final int activeTicks;
    private final int disabledTicks;

    public IntervalCondition(int activeTicks, int disabledTicks) {
        this.activeTicks = activeTicks;
        this.disabledTicks = disabledTicks;
    }

    @Override
    public void registerAbilityProperties(AbilityInstance entry, PropertyManager manager) {
        manager.register(TICKS, 0);
        manager.register(ACTIVE, false);
    }

    @Override
    public boolean active(DataContext context) {
        var entity = context.get(DataContextType.ENTITY);
        var entry = context.get(DataContextType.ABILITY);

        if (entity == null || entry == null) {
            return false;
        }

        var active = Objects.requireNonNull(entry).getProperty(ACTIVE);
        var ticks = entry.getProperty(TICKS);

        var maxTicks = active ? this.activeTicks : this.disabledTicks;
        if (ticks < maxTicks) {
            entry.setUniqueProperty(TICKS, ticks + 1);
        } else {
            entry.setUniqueProperty(TICKS, 0);
            entry.setUniqueProperty(ACTIVE, !active);
            active = !active;
        }

        return active;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return ConditionSerializers.INTERVAL.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Integer> ACTIVE_TICKS = new IntegerProperty("active_ticks").configurable("Determines for how many ticks the condition will be active");
        public static final PalladiumProperty<Integer> DISABLED_TICKS = new IntegerProperty("disabled_ticks").configurable("Determines for how many ticks the condition will be disabled");

        public Serializer() {
            this.withProperty(ACTIVE_TICKS, 20);
            this.withProperty(DISABLED_TICKS, 20);
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.DATA;
        }

        @Override
        public Condition make(JsonObject json) {
            return new IntervalCondition(getProperty(json, ACTIVE_TICKS), getProperty(json, DISABLED_TICKS));
        }

        @Override
        public String getDocumentationDescription() {
            return "Allows you to set an amount of ticks the ability will be active and an amount of ticks the ability will be disabled. The ability will be active for the first amount of ticks and then disabled for the second amount of ticks and so on.";
        }
    }
}
