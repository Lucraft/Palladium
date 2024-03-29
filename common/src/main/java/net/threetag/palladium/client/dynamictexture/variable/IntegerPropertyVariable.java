package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import team.unnamed.mocha.MochaEngine;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IntegerPropertyVariable extends AbstractIntegerTextureVariable {

    private final String propertyKey;

    public IntegerPropertyVariable(String propertyKey, List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        super(operations, function);
        this.propertyKey = propertyKey;
    }

    @Override
    public int getNumber(DataContext context) {
        AtomicInteger result = new AtomicInteger(0);
        EntityPropertyHandler.getHandler(context.getEntity()).ifPresent(handler -> {
            PalladiumProperty<?> property = handler.getPropertyByName(this.propertyKey);

            if (property instanceof IntegerProperty integerProperty) {
                result.set(handler.get(integerProperty));
            }
        });

        return result.get();
    }

    public static class Serializer implements ITextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            String function = GsonHelper.getAsString(json, "function", null);

            return new IntegerPropertyVariable(
                    GsonHelper.getAsString(json, "property"),
                    AbstractIntegerTextureVariable.parseOperations(json),
                    function != null ? MochaEngine.createStandard().compile(function, MoLangIntFunction.class) : null);
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns the value of an integer property within the player. The math operations can be arranged in any order and are fully optional!";
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Integer Property");

            builder.addProperty("property", String.class)
                    .description("Name of the property you want the value from.")
                    .required().exampleJson(new JsonPrimitive("example_property"));

            AbstractIntegerTextureVariable.addDocumentationFields(builder);
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("integer_property");
        }
    }
}
