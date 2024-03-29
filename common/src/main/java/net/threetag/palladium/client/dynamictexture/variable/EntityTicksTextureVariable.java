package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.util.context.DataContext;
import team.unnamed.mocha.MochaEngine;

import java.util.List;

public class EntityTicksTextureVariable extends AbstractIntegerTextureVariable {

    public EntityTicksTextureVariable(List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        super(operations, function);
    }

    @Override
    public int getNumber(DataContext context) {
        var entity = context.getEntity();
        return entity != null ? entity.tickCount : 0;
    }

    public static class Serializer implements ITextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            String function = GsonHelper.getAsString(json, "function", null);

            return new EntityTicksTextureVariable(AbstractIntegerTextureVariable.parseOperations(json),
                    function != null ? MochaEngine.createStandard().compile(function, MoLangIntFunction.class) : null);
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns the tick count of the entity. The math operations can be arranged in any order and are fully optional!";
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Entity Ticks");
            AbstractIntegerTextureVariable.addDocumentationFields(builder);
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("entity_ticks");
        }
    }

}
