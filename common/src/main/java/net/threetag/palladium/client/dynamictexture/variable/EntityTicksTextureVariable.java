package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.util.context.DataContext;

import java.util.List;

public class EntityTicksTextureVariable extends AbstractIntegerTextureVariable {

    public EntityTicksTextureVariable(List<Pair<Operation, Integer>> operations) {
        super(operations);
    }

    @Override
    public int getNumber(DataContext context) {
        var entity = context.getEntity();
        return entity != null ? entity.tickCount : 0;
    }

    public static class Serializer implements TextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            return new EntityTicksTextureVariable(AbstractIntegerTextureVariable.parseOperations(json));
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
