package net.threetag.palladium.power.ability;

import dev.architectury.core.RegistryEntry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.DocumentationBuilder;
import net.threetag.palladium.documentation.IDefaultDocumentedConfigurable;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.PowerManager;
import net.threetag.palladium.util.icon.IIcon;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

import java.util.*;
import java.util.stream.Collectors;

public class Ability extends RegistryEntry<Ability> implements IDefaultDocumentedConfigurable {

    public static final ResourceKey<Registry<Ability>> RESOURCE_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Palladium.MOD_ID, "abilities"));
    public static final Registrar<Ability> REGISTRY = Registries.get(Palladium.MOD_ID).builder(RESOURCE_KEY.location(), new Ability[0]).build();
    public static final PalladiumProperty<Component> TITLE = new ComponentProperty("title").configurable("Allows you to set a custom title for this ability");
    public static final PalladiumProperty<IIcon> ICON = new IconProperty("icon").configurable("Icon for the ability");
    public static final PalladiumProperty<AbilityColor> COLOR = new AbilityColorProperty("bar_color").configurable("Changes the color of the ability in the ability bar");

    final PropertyManager propertyManager = new PropertyManager();

    public Ability() {
        this.withProperty(ICON, new ItemIcon(Items.BLAZE_ROD));
        this.withProperty(TITLE, null);
        this.withProperty(COLOR, AbilityColor.LIGHT_GRAY);
    }

    public void tick(LivingEntity entity, AbilityEntry entry, IPowerHolder holder, boolean enabled) {

    }

    public void firstTick(LivingEntity entity, AbilityEntry entry, IPowerHolder holder, boolean enabled) {

    }

    public void lastTick(LivingEntity entity, AbilityEntry entry, IPowerHolder holder, boolean enabled) {

    }

    public <T> Ability withProperty(PalladiumProperty<T> data, T value) {
        this.propertyManager.register(data, value);
        return this;
    }

    public static Collection<AbilityEntry> getEntries(LivingEntity entity) {
        List<AbilityEntry> entries = new ArrayList<>();
        PowerManager.getPowerHandler(entity).getPowerHolders().values().stream().map(holder -> holder.getAbilities().values()).forEach(entries::addAll);
        return entries;
    }

    public static Collection<AbilityEntry> getEntries(LivingEntity entity, Ability ability) {
        List<AbilityEntry> entries = new ArrayList<>();
        PowerManager.getPowerHandler(entity).getPowerHolders().values().stream().map(holder -> holder.getAbilities().values().stream().filter(entry -> entry.getConfiguration().getAbility() == ability).collect(Collectors.toList())).forEach(entries::addAll);
        return entries;
    }

    public static Collection<AbilityEntry> getEnabledEntries(LivingEntity entity, Ability ability) {
        List<AbilityEntry> entries = new ArrayList<>();
        PowerManager.getPowerHandler(entity).getPowerHolders().values().stream().map(holder -> holder.getAbilities().values().stream().filter(entry -> entry.isEnabled() && entry.getConfiguration().getAbility() == ability).collect(Collectors.toList())).forEach(entries::addAll);
        return entries;
    }

    public static DocumentationBuilder documentationBuilder() {
        return new DocumentationBuilder(new ResourceLocation(Palladium.MOD_ID, "abilities"), "Abilities")
                .add(DocumentationBuilder.heading("Abilities"))
                .addDocumentationSettings(REGISTRY.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
    }

    @Override
    public PropertyManager getPropertyManager() {
        return this.propertyManager;
    }

    @Override
    public ResourceLocation getId() {
        return REGISTRY.getId(this);
    }

    @Override
    public String getTitle() {
        return new TranslatableComponent("ability." + this.getId().getNamespace() + "." + this.getId().getPath()).getString();
    }
}