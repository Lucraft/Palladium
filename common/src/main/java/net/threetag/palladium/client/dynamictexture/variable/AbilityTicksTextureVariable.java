package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.json.GsonUtil;

import java.util.List;

public class AbilityTicksTextureVariable extends AbstractIntegerTextureVariable {

    private final AbilityReference reference;

    public AbilityTicksTextureVariable(ResourceLocation powerId, String abilityId, List<Pair<Operation, Integer>> operations) {
        super(operations);
        this.reference = new AbilityReference(powerId, abilityId);
    }

    @Override
    public int getNumber(DataContext context) {
        var livingEntity = context.getLivingEntity();
        if (livingEntity != null) {
            AbilityInstance entry = this.reference.getInstance(livingEntity);

            if (entry == null) {
                return 0;
            }

            return entry.getEnabledTicks();
        }
        return 0;
    }

    public static class Serializer implements TextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            return new AbilityTicksTextureVariable(
                    GsonUtil.getAsResourceLocation(json, "power"),
                    GsonHelper.getAsString(json, "ability"),
                    AbstractIntegerTextureVariable.parseOperations(json));
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns the tick count of an ability being enabled. The math operations can be arranged in any order and are fully optional!";
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Ability Ticks");

            builder.addProperty("power", ResourceLocation.class)
                    .description("ID of the power the ability is in.")
                    .required().exampleJson(new JsonPrimitive("example:power_id"));

            builder.addProperty("ability", String.class)
                    .description("Key of the ability that is being looked for.")
                    .required().exampleJson(new JsonPrimitive("ability_key"));

            AbstractIntegerTextureVariable.addDocumentationFields(builder);
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("ability_ticks");
        }
    }

}
