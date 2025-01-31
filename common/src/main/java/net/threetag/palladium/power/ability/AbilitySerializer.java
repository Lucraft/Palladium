package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class AbilitySerializer<T extends Ability> implements Documented<Ability, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<Ability, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), Ability.CODEC)
                .ignore("properties")
                .ignore("state")
                .ignore("energy_bar_usage");
        this.addDocumentation(builder, provider);

        if (builder.getExampleObject() != null) {
            builder.setName(builder.getExampleObject().getDisplayName().getString());
        }

        return builder;
    }

    public void addDocumentation(CodecDocumentationBuilder<Ability, T> builder, HolderLookup.Provider provider) {

    }
}
