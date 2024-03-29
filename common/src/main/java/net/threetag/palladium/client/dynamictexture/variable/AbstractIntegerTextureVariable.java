package net.threetag.palladium.client.dynamictexture.variable;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.util.context.DataContext;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.compiled.Named;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractIntegerTextureVariable implements ITextureVariable {

    private final List<Pair<Operation, Integer>> operations;
    private final MoLangIntFunction function;

    public AbstractIntegerTextureVariable(List<Pair<Operation, Integer>> operations, MoLangIntFunction function) {
        this.operations = operations;
        this.function = function;
    }

    @Override
    public Object get(DataContext context) {
        int i = this.getNumber(context);
        for (Pair<Operation, Integer> pair : operations) {
            i = pair.getFirst().function.apply(i, pair.getSecond());
        }

        if (this.function != null) {
            i = this.function.get(i);
            System.out.println(i);
        }

        return i;
    }

    public abstract int getNumber(DataContext context);

    public static List<Pair<Operation, Integer>> parseOperations(JsonObject json) {
        List<Pair<Operation, Integer>> operations = new LinkedList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Operation operation = Operation.getOperationByName(entry.getKey());

            if (operation != null) {
                operations.add(Pair.of(operation, entry.getValue().getAsInt()));
            }
        }
        return operations;
    }

    public interface MoLangIntFunction extends MochaCompiledFunction {

        int get(@Named("value") int value);

    }

    public static void addDocumentationFields(JsonDocumentationBuilder builder) {
        builder.addProperty("add", Integer.class)
                .description("This value will be added on top of the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(5));
        builder.addProperty("subtract", Integer.class)
                .description("This value will be subtracted from the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(3));
        builder.addProperty("multiply", Integer.class)
                .description("This value will be subtracted with the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(10));
        builder.addProperty("divide", Integer.class)
                .description("This value will be divided to the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(10));
        builder.addProperty("min", Integer.class)
                .description("Using this value will set a minimum limit for the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(1));
        builder.addProperty("max", Integer.class)
                .description("Using this value will set a maximum limit for the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(100));
        builder.addProperty("modulo", Integer.class)
                .description("Using this value will apply a modulo operation to the returned value.")
                .fallback(null)
                .exampleJson(new JsonPrimitive(5));
        builder.addProperty("function", Function.class)
                .description("MoLang function to process the number")
                .fallback(null).exampleJson(JsonNull.INSTANCE);
    }

    public enum Operation {

        ADD("add", Integer::sum),
        SUBTRACT("subtract", (input, subtract) -> input - subtract),
        MULTIPLY("multiply", (input, multiply) -> input * multiply),
        DIVIDE("divide", (input, multiply) -> input / multiply),
        MIN("min", Integer::max),
        MAX("max", Integer::min),
        MODULO("modulo", (input, modulo) -> input % modulo);

        private final String name;
        private final BiFunction<Integer, Integer, Integer> function;

        Operation(String name, BiFunction<Integer, Integer, Integer> function) {
            this.name = name;
            this.function = function;
        }

        public static Operation getOperationByName(String name) {
            for (Operation operation : values()) {
                if (operation.name.equalsIgnoreCase(name)) {
                    return operation;
                }
            }
            return null;
        }
    }

}
