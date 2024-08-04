package net.threetag.palladium.compat.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.addonpack.parser.ItemParser;
import net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager;
import net.threetag.palladium.compat.geckolib.armor.AddonGeoArmorItem;
import net.threetag.palladium.compat.geckolib.renderlayer.GeckoRenderLayer;
import software.bernie.geckolib.GeckoLibConstants;

public class GeckoLibCompat {

    public static void init() {
        ItemParser.registerTypeSerializer(new AddonGeoArmorItem.Parser());
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        PackRenderLayerManager.registerParser(GeckoLibConstants.id("default"), GeckoRenderLayer::parse);
    }

    @ExpectPlatform
    public static AddonGeoArmorItem createArmorItem(ArmorMaterial materialIn, ArmorItem.Type type, Item.Properties builder) {
        throw new AssertionError();
    }

    @Environment(EnvType.CLIENT)
    @ExpectPlatform
    public static void renderFirstPerson(AbstractClientPlayer player, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ModelPart rendererArm, boolean rightArm) {
        throw new AssertionError();
    }



}
