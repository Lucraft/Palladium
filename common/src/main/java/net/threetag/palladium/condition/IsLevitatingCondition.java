package net.threetag.palladium.condition;

import com.google.gson.JsonObject;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;
import net.threetag.palladium.entity.PalladiumPlayerExtension;

public class IsLevitatingCondition extends Condition {

    @Override
    public boolean active(DataContext context) {
        var entity = context.get(DataContextType.ENTITY);

        if (entity == null) {
            return false;
        }

        if (entity instanceof PalladiumPlayerExtension extension) {
            float flight = extension.palladium$getFlightHandler().getFlightAnimation(1F);
            return flight > 0F && flight <= 1F;
        }
        return false;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return ConditionSerializers.IS_LEVITATING.get();
    }

    public static class Serializer extends ConditionSerializer {

        @Override
        public Condition make(JsonObject json) {
            return new IsLevitatingCondition();
        }

        @Override
        public String getDocumentationDescription() {
            return "Checks if the entity is levitating.";
        }
    }
}
