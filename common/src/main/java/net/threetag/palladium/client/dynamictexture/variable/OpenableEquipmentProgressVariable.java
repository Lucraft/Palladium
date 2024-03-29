package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.item.Openable;
import net.threetag.palladium.util.context.DataContext;
import team.unnamed.mocha.MochaEngine;

import java.util.List;

public class OpenableEquipmentProgressVariable extends AbstractIntegerTextureVariable {

    public OpenableEquipmentProgressVariable(List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        super(operations, function);
    }

    @Override
    public int getNumber(DataContext context) {
        var item = context.getItem();

        if (!item.isEmpty() && item.getItem() instanceof Openable openable) {
            return openable.getOpeningProgress(item);
        }

        return 0;
    }

    public static class Serializer implements ITextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            String function = GsonHelper.getAsString(json, "function", null);

            return new OpenableEquipmentProgressVariable(AbstractIntegerTextureVariable.parseOperations(json),
                    function != null ? MochaEngine.createStandard().compile(function, MoLangIntFunction.class) : null);
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Openable Equipment Timer");
            AbstractIntegerTextureVariable.addDocumentationFields(builder);
        }

        @Override
        public String getDocumentationDescription() {
            return "When used in an item-context where the item can be opened, this returns the current progress/timer of the opening process.";
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("openable_equipment_progress");
        }
    }
}
