package net.threetag.palladium.documentation;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.Palladium;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecDocumentationBuilder<T, R extends T> {

    public static final String FOLDER = "palladium/export/documentation";
    public static final Map<ResourceLocation, List<JsonElement>> RESULTS = new HashMap<>();

    private final MapCodec<R> mapCodec;
    private final Codec<T> mainCodec;
    private String name;
    private String description;
    private R exampleObject;
    private JsonElement exampleJson;
    private final Map<String, Entry> entries = new HashMap<>();
    private final List<String> ignoredKeys = new ArrayList<>();

    public CodecDocumentationBuilder(MapCodec<R> mapCodec) {
        this.mapCodec = mapCodec;
        this.mainCodec = null;
    }

    public CodecDocumentationBuilder(MapCodec<R> mapCodec, Codec<T> mainCodec) {
        this.mapCodec = mapCodec;
        this.mainCodec = mainCodec;
    }

    public CodecDocumentationBuilder<T, R> setName(String name) {
        this.name = name;
        return this;
    }

    public CodecDocumentationBuilder<T, R> setDescription(String description) {
        this.description = description;
        return this;
    }

    @SuppressWarnings("unchecked")
    public CodecDocumentationBuilder<T, R> setExampleObject(R exampleObject) {
        this.exampleObject = exampleObject;
        Codec<T> codec = this.mainCodec != null ? this.mainCodec : (Codec<T>) this.mapCodec.codec();
        this.exampleJson = codec.encodeStart(JsonOps.INSTANCE, exampleObject).getOrThrow();
        return this;
    }

    public CodecDocumentationBuilder<T, R> setExampleJson(JsonElement exampleJson) {
        this.exampleJson = exampleJson;
        return this;
    }

    public CodecDocumentationBuilder<T, R> addToExampleJson(String key, JsonElement jsonElement) {
        if (this.exampleJson == null || !this.exampleJson.isJsonObject()) {
            this.exampleJson = new JsonObject();
        }

        ((JsonObject) this.exampleJson).add(key, jsonElement);
        return this;
    }

    public R getExampleObject() {
        return this.exampleObject;
    }

    public JsonElement getExampleJson() {
        return this.exampleJson;
    }

    public CodecDocumentationBuilder<T, R> ignore(String key) {
        this.ignoredKeys.add(key);
        return this;
    }

    public CodecDocumentationBuilder<T, R> add(String key, JsonElement type, String description) {
        this.entries.put(key, new Entry(key, type, description, true, null));
        return this;
    }

    public CodecDocumentationBuilder<T, R> addOptional(String key, JsonElement type, String description) {
        this.entries.put(key, new Entry(key, type, description, false, null));
        return this;
    }

    public CodecDocumentationBuilder<T, R> addOptional(String key, JsonElement type, String description, Object fallback) {
        this.entries.put(key, new Entry(key, type, description, false, fallback));
        return this;
    }

    public void build(ResourceKey<?> id) {
        var json = new JsonObject();

        json.addProperty("namespace", id.location().getNamespace());
        json.addProperty("path", id.location().getPath());

        if (this.name != null && !this.name.isEmpty()) {
            json.addProperty("name", this.name);
        }

        if (this.description != null && !this.description.isEmpty()) {
            json.addProperty("description", this.description);
        }

        var fields = new JsonArray();

        this.mapCodec.keys(JsonOps.INSTANCE).map(JsonElement::getAsString).distinct().forEach(key -> {
            if (!this.ignoredKeys.contains(key)) {
                if (!this.entries.containsKey(key)) {
                    Palladium.LOGGER.error("No documentation entry {} found for {}", key, id.toString());
                } else {
                    var documented = this.entries.get(key);
                    var entryJson = new JsonObject();
                    entryJson.addProperty("key", key);
                    entryJson.add("type", documented.type);
                    entryJson.addProperty("description", documented.description);
                    entryJson.addProperty("required", documented.required);
                    entryJson.add("fallback", toJsonElement(documented.fallback));
                    fields.add(entryJson);
                }
            }
        });

        json.add("fields", fields);

        if (this.exampleJson != null) {
            json.add("example", orderJsonObject(this.exampleJson));
        }

        RESULTS.computeIfAbsent(id.registry(), x -> new ArrayList<>()).add(json);
    }

    public static void createFiles() {
        for (Map.Entry<ResourceLocation, List<JsonElement>> e : RESULTS.entrySet()) {
            try {
                var jsonArray = new JsonArray();
                for (JsonElement j : e.getValue()) {
                    jsonArray.add(j);
                }

                BufferedWriter bw = getBufferedWriter(e.getKey().getNamespace(), e.getKey().getPath());
                bw.write(jsonArray.toString());
                bw.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static @NotNull BufferedWriter getBufferedWriter(String path, String filename) throws IOException {
        Path folder = Platform.getGameFolder().resolve(FOLDER).resolve(path);
        var file = folder.toFile();
        if (!file.exists() && !file.mkdirs())
            throw new RuntimeException("Could not create palladium export directory! Please create the directory yourself, or make sure the name is not taken by a file and you have permission to create directories.");

        file = folder.resolve(filename + ".json").toFile();
        return new BufferedWriter(new FileWriter(file));
    }

    private static JsonElement toJsonElement(Object object) {
        return switch (object) {
            case null -> JsonNull.INSTANCE;
            case Number n -> new JsonPrimitive(n);
            case Boolean b -> new JsonPrimitive(b);
            case String s -> new JsonPrimitive(s);
            case Character c -> new JsonPrimitive(c);
            default -> new JsonPrimitive(object.toString());
        };
    }

    private static JsonElement orderJsonObject(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            JsonObject orderedJson = new JsonObject();

            if (json.has("type")) {
                orderedJson.add("type", json.get("type"));
            }

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (!entry.getKey().equals("type")) {
                    orderedJson.add(entry.getKey(), orderJsonObject(entry.getValue()));
                }
            }

            return orderedJson;
        } else {
            return jsonElement;
        }
    }

    private record Entry(String key, JsonElement type, String description, boolean required, Object fallback) {

    }
}
