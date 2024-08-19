package net.threetag.palladium;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.block.Block;
import net.threetag.palladium.accessory.Accessory;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.block.IAddonBlock;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.client.PalladiumKeyMappings;
import net.threetag.palladium.client.dynamictexture.DynamicTextureManager;
import net.threetag.palladium.client.energybeam.EnergyBeamManager;
import net.threetag.palladium.client.model.SuitStandBasePlateModel;
import net.threetag.palladium.client.model.SuitStandModel;
import net.threetag.palladium.client.model.animation.*;
import net.threetag.palladium.client.particleemitter.ParticleEmitterManager;
import net.threetag.palladium.client.renderer.entity.CustomProjectileRenderer;
import net.threetag.palladium.client.renderer.entity.EffectEntityRenderer;
import net.threetag.palladium.client.renderer.entity.SuitStandRenderer;
import net.threetag.palladium.client.renderer.entity.TrailSegmentEntityRenderer;
import net.threetag.palladium.client.renderer.item.armor.ArmorRendererManager;
import net.threetag.palladium.client.renderer.renderlayer.AccessoryRenderLayer;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerRenderer;
import net.threetag.palladium.client.renderer.trail.TrailRendererManager;
import net.threetag.palladium.client.screen.AbilityBarRenderer;
import net.threetag.palladium.client.screen.AccessoryScreen;
import net.threetag.palladium.client.screen.AddonPackLogScreen;
import net.threetag.palladium.client.screen.components.IconButton;
import net.threetag.palladium.client.screen.power.PowersScreen;
import net.threetag.palladium.compat.geckolib.GeckoLibCompat;
import net.threetag.palladium.energy.EnergyHelper;
import net.threetag.palladium.entity.PalladiumEntityTypes;
import net.threetag.palladium.event.PalladiumClientEvents;
import net.threetag.palladium.item.BaseEnergyItem;
import net.threetag.palladium.item.Openable;
import net.threetag.palladium.item.types.BowItemType;
import net.threetag.palladium.item.types.CrossbowItemType;
import net.threetag.palladium.item.types.ShieldItemType;
import net.threetag.palladium.power.ability.AbilityClientEventHandler;
import net.threetag.palladium.power.ability.GuiOverlayAbility;
import net.threetag.palladium.util.SupporterHandler;
import net.threetag.palladium.util.icon.Icon;
import net.threetag.palladium.util.icon.TexturedIcon;
import net.threetag.palladiumcore.event.LifecycleEvents;
import net.threetag.palladiumcore.event.ScreenEvents;
import net.threetag.palladiumcore.registry.ReloadListenerRegistry;
import net.threetag.palladiumcore.registry.client.EntityRendererRegistry;
import net.threetag.palladiumcore.registry.client.GuiLayerRegistry;
import net.threetag.palladiumcore.registry.client.ItemPropertyRegistry;
import net.threetag.palladiumcore.registry.client.RenderTypeRegistry;
import net.threetag.palladiumcore.util.Platform;

public class PalladiumClient {

    public static final Icon ICON = new TexturedIcon(Palladium.id("textures/icon/palladium.png"));

    @SuppressWarnings("unchecked")
    public static void init() {
        PalladiumKeyMappings.init();
        PowersScreen.register();
        AccessoryScreen.addButton();
        SupporterHandler.clientInit();
        setupDevLogButton();
        AbilityClientEventHandler.init();

        // During Setup
        LifecycleEvents.SETUP.register(() -> {
            blockRenderTypes();
            itemModelPredicates();
        });

        // Entity Renderers
        EntityRendererRegistry.register(PalladiumEntityTypes.EFFECT, EffectEntityRenderer::new);
        EntityRendererRegistry.register(PalladiumEntityTypes.TRAIL_SEGMENT, TrailSegmentEntityRenderer::new);
        EntityRendererRegistry.register(PalladiumEntityTypes.CUSTOM_PROJECTILE, CustomProjectileRenderer::new);
        EntityRendererRegistry.register(PalladiumEntityTypes.SUIT_STAND, SuitStandRenderer::new);
        EntityRendererRegistry.registerModelLayer(SuitStandModel.MODEL_LAYER_LOCATION, SuitStandModel::createBodyLayer);
        EntityRendererRegistry.registerModelLayer(SuitStandBasePlateModel.MODEL_LAYER_LOCATION, SuitStandBasePlateModel::createLayer);

        // Entity Render Layers
        EntityRendererRegistry.addRenderLayer((e) -> true, renderLayerParent -> new PackRenderLayerRenderer((RenderLayerParent<Entity, EntityModel<Entity>>) renderLayerParent));
        EntityRendererRegistry.addRenderLayerToPlayer(renderLayerParent -> new AccessoryRenderLayer((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderLayerParent));

        // Animations
        PalladiumClientEvents.REGISTER_ANIMATIONS.register(registry -> {
            registry.accept(Palladium.id("hovering"), new HoveringAnimation(-30));
            registry.accept(Palladium.id("levitation"), new LevitationAnimation(-20));
            registry.accept(Palladium.id("flight"), new FlightAnimation(-10));
            registry.accept(Palladium.id("aim"), new AimAnimation(100));
        });

        // Overlay Renderer
        GuiLayerRegistry.register(Palladium.id("ability_bar"), new AbilityBarRenderer());
        GuiLayerRegistry.register(Palladium.id("palladium/gui_overlay_abilities"), new GuiOverlayAbility.Renderer());

        // Reload Listeners
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("dynamic_textures"), new DynamicTextureManager());
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("pack_render_layers"), new PackRenderLayerManager());
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("armor_renderers"), new ArmorRendererManager());
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("accessory_renderers"), new Accessory.ReloadManager());
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("humanoid_animations"), PalladiumAnimationRegistry.INSTANCE);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("trail_renderers"), TrailRendererManager.INSTANCE);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("particle_emitters"), ParticleEmitterManager.INSTANCE);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, Palladium.id("energy_beams"), EnergyBeamManager.INSTANCE);

        // Gecko
        if (Platform.isModLoaded("geckolib")) {
            GeckoLibCompat.initClient();
        }
    }

    public static void blockRenderTypes() {
        RenderTypeRegistry.registerBlock(RenderType.cutout(),
                PalladiumBlocks.HEART_SHAPED_HERB.get(),
                PalladiumBlocks.POTTED_HEART_SHAPED_HERB.value(),
                PalladiumBlocks.SMALL_REDSTONE_FLUX_CRYSTAL_BUD.value(),
                PalladiumBlocks.MEDIUM_REDSTONE_FLUX_CRYSTAL_BUD.value(),
                PalladiumBlocks.LARGE_REDSTONE_FLUX_CRYSTAL_BUD.value(),
                PalladiumBlocks.REDSTONE_FLUX_CRYSTAL_CLUSTER.value());

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof IAddonBlock addonBlock) {
                var type = addonBlock.getRenderType();

                if (type != null) {
                    if (type.equalsIgnoreCase("solid")) {
                        RenderTypeRegistry.registerBlock(RenderType.solid(), block);
                    } else if (type.equalsIgnoreCase("cutout_mipped")) {
                        RenderTypeRegistry.registerBlock(RenderType.cutoutMipped(), block);
                    } else if (type.equalsIgnoreCase("cutout")) {
                        RenderTypeRegistry.registerBlock(RenderType.cutout(), block);
                    } else if (type.equalsIgnoreCase("translucent")) {
                        RenderTypeRegistry.registerBlock(RenderType.translucent(), block);
                    }
                }
            }
        }
    }

    public static void itemModelPredicates() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BaseEnergyItem) {
                ItemPropertyRegistry.register(item, Palladium.id("energy"), (itemStack, clientLevel, livingEntity, i) -> {
                    var storage = EnergyHelper.getFromItem(itemStack);
                    return storage != null ? Math.round(13F * storage.getEnergyAmount() / (float) storage.getEnergyCapacity()) : 0;
                });
                ItemPropertyRegistry.register(item, Palladium.id("charged"), (itemStack, clientLevel, livingEntity, i) -> {
                    var stored = EnergyHelper.getEnergyStoredInItem(itemStack);
                    return stored > 0 ? 1F : 0F;
                });
            }

            if (item instanceof Openable openable) {
                ItemPropertyRegistry.register(item, Palladium.id("opened"), (itemStack, clientLevel, livingEntity, i) -> {
                    var max = openable.getOpeningTime(itemStack);

                    if (max > 0) {
                        return (float) openable.getOpeningProgress(itemStack) / (float) max;
                    } else {
                        return openable.isOpen(itemStack) ? 1F : 0F;
                    }
                });
            }

            if (item instanceof ShieldItemType.ExtShieldItem) {
                ItemPropertyRegistry.register(item, ResourceLocation.withDefaultNamespace("blocking"), (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
            }

            if (item instanceof BowItemType.ExtBowItem) {
                ItemPropertyRegistry.register(item, ResourceLocation.withDefaultNamespace("pull"), (itemStack, clientLevel, livingEntity, i) -> {
                    if (livingEntity == null) {
                        return 0.0F;
                    } else {
                        return livingEntity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / 20.0F;
                    }
                });

                ItemPropertyRegistry.register(
                        item,
                        ResourceLocation.withDefaultNamespace("pulling"),
                        (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
                );
            }

            if (item instanceof CrossbowItemType.ExtCrossbowItem) {
                ItemPropertyRegistry.register(
                        item,
                        ResourceLocation.withDefaultNamespace("pull"),
                        (itemStack, clientLevel, livingEntity, i) -> {
                            if (livingEntity == null) {
                                return 0.0F;
                            } else {
                                return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack, livingEntity);
                            }
                        }
                );
                ItemPropertyRegistry.register(
                        item,
                        ResourceLocation.withDefaultNamespace("pulling"),
                        (itemStack, clientLevel, livingEntity, i) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F
                );
                ItemPropertyRegistry.register(
                        item,
                        ResourceLocation.withDefaultNamespace("charged"),
                        (itemStack, clientLevel, livingEntity, i) -> CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F
                );
                ItemPropertyRegistry.register(
                        item,
                        ResourceLocation.withDefaultNamespace("firework"),
                        (itemStack, clientLevel, livingEntity, i) -> {
                            ChargedProjectiles chargedProjectiles = (ChargedProjectiles) itemStack.get(DataComponents.CHARGED_PROJECTILES);
                            return chargedProjectiles != null && chargedProjectiles.contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
                        }
                );
            }
        }
    }

    public static void setupDevLogButton() {
        ScreenEvents.INIT_POST.register((screen) -> {
            if (PalladiumConfig.Client.ADDON_PACK_DEV_MODE.get() && (screen instanceof TitleScreen || screen instanceof PauseScreen)) {
                screen.addRenderableWidget(IconButton.builder(ICON, button ->
                                Minecraft.getInstance().setScreen(new AddonPackLogScreen(AddonPackLog.getEntries(), screen)))
                        .pos(screen.width - 30, 10)
                        .tooltip(Tooltip.create(Component.translatable("gui.palladium.addon_pack_log"))).build());
            }
        });
    }

}
