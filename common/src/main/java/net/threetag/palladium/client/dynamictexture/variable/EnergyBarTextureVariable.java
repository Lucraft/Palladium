package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBar;
import net.threetag.palladium.power.energybar.EnergyBarReference;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.json.GsonUtil;
import team.unnamed.mocha.MochaEngine;

import java.util.List;

public class EnergyBarTextureVariable extends AbstractIntegerTextureVariable {

    private final EnergyBarReference reference;

    public EnergyBarTextureVariable(ResourceLocation powerId, String energyBarName, List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        super(operations, function);
        this.reference = new EnergyBarReference(powerId, energyBarName);
    }

    @Override
    public int getNumber(DataContext context) {
        var livingEntity = context.getLivingEntity();
        if (livingEntity != null) {
            EnergyBar bar = this.reference.getEntry(livingEntity);

            if (bar == null) {
                return 0;
            }

            return bar.get();
        }
        return 0;
    }

    public static class Serializer implements ITextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            String function = GsonHelper.getAsString(json, "function", null);

            return new EnergyBarTextureVariable(
                    GsonUtil.getAsResourceLocation(json, "power"),
                    GsonHelper.getAsString(json, "energy_bar"),
                    AbstractIntegerTextureVariable.parseOperations(json),
                    function != null ? MochaEngine.createStandard().compile(function, MoLangIntFunction.class) : null);
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns the value of an energy bar. The math operations can be arranged in any order and are fully optional!";
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Energy Bar");
            
            builder.addProperty("power", ResourceLocation.class)
                    .description("ID of the power the energy bar is in.")
                    .required().exampleJson(new JsonPrimitive("example:power_id"));

            builder.addProperty("energy_bar", String.class)
                    .description("Name of the energy bar that is being looked for.")
                    .required().exampleJson(new JsonPrimitive("energy_bar_name"));

            AbstractIntegerTextureVariable.addDocumentationFields(builder);
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("energy_bar");
        }
    }

}
