package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.power.ability.AbilityEntry;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.AnimationTimer;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.json.GsonUtil;
import team.unnamed.mocha.MochaEngine;

import java.util.List;

public class AnimationTimerAbilityVariable extends AbstractIntegerTextureVariable {

    private final AbilityReference reference;

    public AnimationTimerAbilityVariable(ResourceLocation powerId, String abilityId, List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        super(operations, function);
        this.reference = new AbilityReference(powerId, abilityId);
    }

    @Override
    public int getNumber(DataContext context) {
        var livingEntity = context.getLivingEntity();
        if (livingEntity != null) {
            AbilityEntry entry = this.reference.getEntry(livingEntity);

            if (entry == null || !(entry.getConfiguration().getAbility() instanceof AnimationTimer animationTimer)) {
                return 0;
            }

            return (int) animationTimer.getAnimationTimer(entry, 1F);
        }

        return 0;
    }

    public static class Serializer implements ITextureVariableSerializer {

        @Override
        public ITextureVariable parse(JsonObject json) {
            String function = GsonHelper.getAsString(json, "function", null);

            return new AnimationTimerAbilityVariable(
                    GsonUtil.getAsResourceLocation(json, "power"),
                    GsonHelper.getAsString(json, "ability"),
                    AbstractIntegerTextureVariable.parseOperations(json),
                    function != null ? MochaEngine.createStandard().compile(function, MoLangIntFunction.class) : null);
        }

        @Override
        public String getDocumentationDescription() {
            return "Returns the timer of an animation timer ability. The math operations can be arranged in any order and are fully optional!";
        }

        @Override
        public void addDocumentationFields(JsonDocumentationBuilder builder) {
            builder.setTitle("Animation Timer Ability");
            
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
            return Palladium.id("animation_timer_ability");
        }
    }
}
