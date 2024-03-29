package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.util.context.DataContext;
import team.unnamed.mocha.MochaEngine;

import java.util.List;

public class ObjectiveScoreTextureVariable extends AbstractIntegerTextureVariable {

    private final String objectiveName;

    public ObjectiveScoreTextureVariable(String objectiveName, List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        super(operations, function);
        this.objectiveName = objectiveName;
    }

    @Override
    public int getNumber(DataContext context) {
        var level = context.getLevel();
        var entity = context.getEntity();

        if(level != null && entity != null) {
            var objective = level.getScoreboard().getObjective(this.objectiveName);

            if(objective != null) {
                if (level.getScoreboard().hasPlayerScore(entity.getScoreboardName(), objective)) {
                    return level.getScoreboard().getOrCreatePlayerScore(entity.getScoreboardName(), objective).getScore();
                }
            }
        }

        return 0;
    }

    public static class Serializer implements ITextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            String function = GsonHelper.getAsString(json, "function", null);

            return new ObjectiveScoreTextureVariable(
                    GsonHelper.getAsString(json, "objective"),
                    AbstractIntegerTextureVariable.parseOperations(json),
                    function != null ? MochaEngine.createStandard().compile(function, MoLangIntFunction.class) : null);
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns the value of a score of an entity in the scoreboard for the given objective. IF YOU USE THIS, MAKE A 'tracked_score.json' AND PUT THE OBJECTIVE NAME IN IT, MORE ON THE WIKI!";
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Objective Score");

            builder.addProperty("objective", String.class)
                    .description("Name of the objective.")
                    .required().exampleJson(new JsonPrimitive("objective_name"));

            AbstractIntegerTextureVariable.addDocumentationFields(builder);
        }

        @Override
        public ResourceLocation getId() {
            return Palladium.id("objective_score");
        }
    }
}
