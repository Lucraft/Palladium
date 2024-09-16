package net.threetag.palladium;

import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.accessory.Accessories;
import net.threetag.palladium.accessory.Accessory;
import net.threetag.palladium.addonpack.parser.*;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.block.entity.PalladiumBlockEntityTypes;
import net.threetag.palladium.client.dynamictexture.DynamicTextureManager;
import net.threetag.palladium.client.energybeam.EnergyBeamManager;
import net.threetag.palladium.client.renderer.trail.TrailRendererManager;
import net.threetag.palladium.command.AbilityCommand;
import net.threetag.palladium.command.EnergyBarCommand;
import net.threetag.palladium.command.PalladiumEntitySelectorOptions;
import net.threetag.palladium.command.SuperpowerCommand;
import net.threetag.palladium.compat.geckolib.GeckoLibCompat;
import net.threetag.palladium.compat.pehkui.PehkuiCompat;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladium.condition.ConditionSerializers;
import net.threetag.palladium.documentation.HTMLBuilder;
import net.threetag.palladium.entity.PalladiumAttributes;
import net.threetag.palladium.entity.PalladiumEntityTypes;
import net.threetag.palladium.entity.effect.EntityEffects;
import net.threetag.palladium.entity.value.EntityDependentNumberTypes;
import net.threetag.palladium.event.PalladiumEvents;
import net.threetag.palladium.item.PalladiumArmorMaterials;
import net.threetag.palladium.item.PalladiumCreativeModeTabs;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.network.PalladiumNetwork;
import net.threetag.palladium.power.ItemPowerManager;
import net.threetag.palladium.power.PowerEventHandler;
import net.threetag.palladium.power.SuitSetPowerManager;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityEventHandler;
import net.threetag.palladium.power.provider.PowerProviders;
import net.threetag.palladium.registry.PalladiumRegistries;
import net.threetag.palladium.sound.PalladiumSoundEvents;
import net.threetag.palladium.util.SupporterHandler;
import net.threetag.palladium.util.icon.IconSerializer;
import net.threetag.palladium.util.icon.IconSerializers;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import net.threetag.palladium.util.property.PalladiumProperties;
import net.threetag.palladium.world.PalladiumFeatures;
import net.threetag.palladium.world.TrackedScoresManager;
import net.threetag.palladiumcore.event.CommandEvents;
import net.threetag.palladiumcore.event.LifecycleEvents;
import net.threetag.palladiumcore.event.PlayerEvents;
import net.threetag.palladiumcore.util.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class Palladium {

    public static final String MOD_ID = "palladium";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        PalladiumRegistries.init();

        PalladiumBlocks.BLOCKS.register();
        PalladiumBlockEntityTypes.BLOCK_ENTITIES.register();
        PalladiumCreativeModeTabs.TABS.register();
        PalladiumArmorMaterials.ARMOR_MATERIALS.register();
        PalladiumDataComponents.DATA_COMPONENTS.register();
        PalladiumItems.ITEMS.register();
        AbilitySerializers.ABILITIES.register();
        ConditionSerializers.CONDITION_SERIALIZERS.register();
        PowerProviders.PROVIDERS.register();
        IconSerializers.ICON_SERIALIZERS.register();
        PalladiumFeatures.FEATURES.register();
        PalladiumAttributes.ATTRIBUTES.register();
        EntityEffects.EFFECTS.register();
        PalladiumEntityTypes.ENTITIES.register();
        PalladiumSoundEvents.SOUNDS.register();
        Accessories.ACCESSORIES.register();
        EntityDependentNumberTypes.TYPES.register();

        // Init before addonpack stuff is loaded, so new item type is registered
        if (Platform.isModLoaded("geckolib")) {
            GeckoLibCompat.init();
        }

        PalladiumItems.init();
        PalladiumNetwork.init();
        PalladiumEntityTypes.init();
        PowerEventHandler.init();
        ItemPowerManager.init();
        SuitSetPowerManager.init();
        AbilityEventHandler.init();
        AbilitySerializers.init();
        PalladiumProperties.init();
        PalladiumAttributes.init();
        EntityEffects.init();
        SupporterHandler.init();
        PalladiumEntitySelectorOptions.init();
        TrackedScoresManager.init();

        LifecycleEvents.SETUP.register(() -> {
            Palladium.generateDocumentation();

            if (Platform.isModLoaded("pehkui")) {
                PehkuiCompat.init();
            }
        });

        CommandEvents.REGISTER.register((dispatcher, context, selection) -> {
            SuperpowerCommand.register(dispatcher, context);
            AbilityCommand.register(dispatcher, context);
            EnergyBarCommand.register(dispatcher);
        });

        if (!Platform.isProduction()) {
            PalladiumDebug.init();
        }

        // Carry over data from old players to new
        PlayerEvents.CLONE.register((oldPlayer, newPlayer, wasDeath) -> {
            PowerEventHandler.getPowerHandler(oldPlayer).ifPresent(handlerOld -> {
                PowerEventHandler.getPowerHandler(newPlayer).ifPresent(handlerNew -> {
                    handlerNew.fromNBT(handlerOld.toNBT());
                });
            });

            EntityPropertyHandler.getHandler(oldPlayer).ifPresent(handlerOld -> {
                EntityPropertyHandler.getHandler(newPlayer).ifPresent(handlerNew -> {
                    handlerNew.fromNBT(handlerOld.toNBT(true));
                });
            });

            Accessory.getPlayerData(oldPlayer).ifPresent(handlerOld -> {
                Accessory.getPlayerData(newPlayer).ifPresent(handlerNew -> {
                    handlerNew.fromNBT(handlerOld.toNBT());
                });
            });
        });
    }

    public static void generateDocumentation() {
        if (Platform.isClient()) {
            Consumer<HTMLBuilder> consumer = HTMLBuilder::save;
            consumer.accept(AbilitySerializer.documentationBuilder());
            consumer.accept(ConditionSerializer.documentationBuilder());
            consumer.accept(CreativeModeTabParser.documentationBuilder());
            consumer.accept(ArmorMaterialParser.documentationBuilder());
            consumer.accept(ToolTierParser.documentationBuilder());
            consumer.accept(BlockParser.documentationBuilder());
            consumer.accept(ItemParser.documentationBuilder());
            consumer.accept(IconSerializer.documentationBuilder());
            consumer.accept(DynamicTextureManager.variableDocumentationBuilder());
            consumer.accept(AccessoryParser.documentationBuilder());
            consumer.accept(AccessorySlotParser.documentationBuilder());
            consumer.accept(TrailRendererManager.documentationBuilder());
            consumer.accept(EnergyBeamManager.documentationBuilder());
            PalladiumEvents.GENERATE_DOCUMENTATION.invoker().generate(consumer);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
